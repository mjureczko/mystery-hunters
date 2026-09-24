#!/usr/bin/env python3
"""
Generates the three dimensional question mark shown in the augmented reality scene.

The mark is a tube swept along the outline of the character plus a ball for the dot, so it is
solid from every side rather than a flat sheet. Sizes are in metres and are baked into the model,
because the scene places the mark at the real distance of the point and lets perspective do the
rest - no scaling in the application.

Run it from the root of the repository:

    python3 tools/question_mark_model.py

Only numpy is needed; the glb container is written by hand.
"""
import json
import struct
import sys

import numpy as np

# ── Shape, in metres ──────────────────────────────────────────────────────────────────────────
TOTAL_HEIGHT = 3.0
TOTAL_WIDTH = 1.0
STROKE_RADIUS = 0.12          # half the thickness of the line the character is drawn with
DOT_RADIUS = 0.16             # the dot is a touch fatter than the stroke, as in most typefaces
DOT_GAP = 0.18                # empty space between the dot and the foot of the stem

# Three times taller than wide, near enough to the proportions of a real question mark that the
# bowl only needs stretching a little.
BOWL_HALF_WIDTH = TOTAL_WIDTH / 2 - STROKE_RADIUS   # bowl plus stroke fills the width exactly
BOWL_HALF_HEIGHT = 0.55
BOWL_CENTRE_Y = TOTAL_HEIGHT - STROKE_RADIUS - BOWL_HALF_HEIGHT
BOWL_FROM_DEGREES = 205.0     # open end of the bowl, on the lower left
BOWL_TO_DEGREES = -35.0       # where the bowl hands over to the tail, on the lower right
STEM_TOP_Y = 1.35

RADIAL_SEGMENTS = 14
BOWL_SEGMENTS = 48
TAIL_SEGMENTS = 28
STEM_SEGMENTS = 10
SPHERE_RINGS = 14
SPHERE_SEGMENTS = 20

# Golden yellow, unlit by metal so it stays readable against any sky.
BASE_COLOUR = [1.0, 0.82, 0.05, 1.0]
METALLIC = 0.0
ROUGHNESS = 0.45


def bowl_points():
    """The round head of the character, swept clockwise from its open end over the top."""
    angles = np.radians(np.linspace(BOWL_FROM_DEGREES, BOWL_TO_DEGREES, BOWL_SEGMENTS))
    return np.stack([
        BOWL_HALF_WIDTH * np.cos(angles),
        BOWL_CENTRE_Y + BOWL_HALF_HEIGHT * np.sin(angles),
        np.zeros_like(angles),
    ], axis=1)


def tail_points(start, start_direction):
    """Smooth run from the end of the bowl down to the top of the stem."""
    end = np.array([0.0, STEM_TOP_Y, 0.0])
    control_one = start + start_direction * 0.38
    control_two = end + np.array([0.0, 0.9, 0.0])
    t = np.linspace(0.0, 1.0, TAIL_SEGMENTS)[1:, None]
    return ((1 - t) ** 3 * start
            + 3 * (1 - t) ** 2 * t * control_one
            + 3 * (1 - t) * t ** 2 * control_two
            + t ** 3 * end)


def stem_points():
    bottom = DOT_RADIUS * 2 + DOT_GAP + STROKE_RADIUS
    ys = np.linspace(STEM_TOP_Y, bottom, STEM_SEGMENTS)[1:]
    return np.stack([np.zeros_like(ys), ys, np.zeros_like(ys)], axis=1)


def centre_line():
    bowl = bowl_points()
    # Direction the bowl is travelling in as it ends, so the tail leaves it without a kink.
    end_angle = np.radians(BOWL_TO_DEGREES)
    direction = np.array([
        BOWL_HALF_WIDTH * np.sin(end_angle),
        -BOWL_HALF_HEIGHT * np.cos(end_angle),
        0.0,
    ])
    direction /= np.linalg.norm(direction)
    return np.concatenate([bowl, tail_points(bowl[-1], direction), stem_points()])


def frames(points):
    """
    Parallel transport frames along the line. Rotating the previous frame onto the new tangent,
    rather than rebuilding it from a fixed up vector, keeps the tube from twisting where the line
    turns back on itself.
    """
    tangents = np.gradient(points, axis=0)
    tangents /= np.linalg.norm(tangents, axis=1, keepdims=True)
    normals = np.zeros_like(points)
    seed = np.array([0.0, 0.0, 1.0])
    if abs(np.dot(seed, tangents[0])) > 0.9:
        seed = np.array([1.0, 0.0, 0.0])
    normals[0] = np.cross(tangents[0], seed)
    normals[0] /= np.linalg.norm(normals[0])
    for i in range(1, len(points)):
        axis = np.cross(tangents[i - 1], tangents[i])
        length = np.linalg.norm(axis)
        if length < 1e-9:
            normals[i] = normals[i - 1]
        else:
            axis = axis / length
            angle = np.arctan2(length, np.dot(tangents[i - 1], tangents[i]))
            n = normals[i - 1]
            normals[i] = (n * np.cos(angle)
                          + np.cross(axis, n) * np.sin(angle)
                          + axis * np.dot(axis, n) * (1 - np.cos(angle)))
        normals[i] /= np.linalg.norm(normals[i])
    binormals = np.cross(tangents, normals)
    return tangents, normals, binormals


def tube(points, radius):
    tangents, normals, binormals = frames(points)
    angles = np.linspace(0, 2 * np.pi, RADIAL_SEGMENTS, endpoint=False)
    ring = np.cos(angles)[:, None] * normals[:, None, :].transpose(0, 1, 2)
    positions, vertex_normals = [], []
    for i in range(len(points)):
        outward = (np.cos(angles)[:, None] * normals[i] + np.sin(angles)[:, None] * binormals[i])
        positions.append(points[i] + outward * radius)
        vertex_normals.append(outward)
    positions = np.concatenate(positions)
    vertex_normals = np.concatenate(vertex_normals)

    faces = []
    for i in range(len(points) - 1):
        for j in range(RADIAL_SEGMENTS):
            a = i * RADIAL_SEGMENTS + j
            b = i * RADIAL_SEGMENTS + (j + 1) % RADIAL_SEGMENTS
            c = a + RADIAL_SEGMENTS
            d = b + RADIAL_SEGMENTS
            faces += [[a, c, b], [b, c, d]]

    # Flat discs closing both ends, so the character does not look hollow when seen end on.
    for index, (centre, normal, sign) in enumerate(
            ((points[0], -tangents[0], -1), (points[-1], tangents[-1], 1))):
        base = len(positions)
        rim = np.arange(RADIAL_SEGMENTS) + (0 if index == 0 else (len(points) - 1) * RADIAL_SEGMENTS)
        positions = np.concatenate([positions, centre[None, :]])
        vertex_normals = np.concatenate([vertex_normals, normal[None, :]])
        for j in range(RADIAL_SEGMENTS):
            a, b = rim[j], rim[(j + 1) % RADIAL_SEGMENTS]
            faces.append([base, a, b] if sign < 0 else [base, b, a])
    return positions, vertex_normals, np.array(faces, dtype=np.uint32)


def sphere(centre, radius):
    positions, normals, faces = [], [], []
    for ring in range(SPHERE_RINGS + 1):
        phi = np.pi * ring / SPHERE_RINGS
        for segment in range(SPHERE_SEGMENTS + 1):
            theta = 2 * np.pi * segment / SPHERE_SEGMENTS
            n = np.array([np.sin(phi) * np.cos(theta), np.cos(phi), np.sin(phi) * np.sin(theta)])
            normals.append(n)
            positions.append(centre + n * radius)
    for ring in range(SPHERE_RINGS):
        for segment in range(SPHERE_SEGMENTS):
            a = ring * (SPHERE_SEGMENTS + 1) + segment
            b = a + SPHERE_SEGMENTS + 1
            faces += [[a, b, a + 1], [a + 1, b, b + 1]]
    return np.array(positions), np.array(normals), np.array(faces, dtype=np.uint32)


def write_glb(path, positions, normals, indices):
    positions = positions.astype(np.float32)
    normals = normals.astype(np.float32)
    indices = indices.astype(np.uint32).reshape(-1)

    blob = positions.tobytes() + normals.tobytes() + indices.tobytes()
    while len(blob) % 4:
        blob += b"\0"

    gltf = {
        "asset": {"version": "2.0", "generator": "mystery-hunters tools/question_mark_model.py"},
        "scene": 0,
        "scenes": [{"nodes": [0]}],
        "nodes": [{"mesh": 0, "name": "QuestionMark"}],
        "meshes": [{
            "name": "QuestionMark",
            "primitives": [{"attributes": {"POSITION": 0, "NORMAL": 1}, "indices": 2, "material": 0}],
        }],
        "materials": [{
            "name": "Yellow",
            "doubleSided": True,
            "pbrMetallicRoughness": {
                "baseColorFactor": BASE_COLOUR,
                "metallicFactor": METALLIC,
                "roughnessFactor": ROUGHNESS,
            },
        }],
        "accessors": [
            {"bufferView": 0, "componentType": 5126, "count": len(positions), "type": "VEC3",
             "min": positions.min(axis=0).tolist(), "max": positions.max(axis=0).tolist()},
            {"bufferView": 1, "componentType": 5126, "count": len(normals), "type": "VEC3"},
            {"bufferView": 2, "componentType": 5125, "count": len(indices), "type": "SCALAR"},
        ],
        "bufferViews": [
            {"buffer": 0, "byteOffset": 0, "byteLength": positions.nbytes, "target": 34962},
            {"buffer": 0, "byteOffset": positions.nbytes, "byteLength": normals.nbytes, "target": 34962},
            {"buffer": 0, "byteOffset": positions.nbytes + normals.nbytes,
             "byteLength": indices.nbytes, "target": 34963},
        ],
        "buffers": [{"byteLength": len(blob)}],
    }

    json_chunk = json.dumps(gltf, separators=(",", ":")).encode()
    while len(json_chunk) % 4:
        json_chunk += b" "

    total = 12 + 8 + len(json_chunk) + 8 + len(blob)
    with open(path, "wb") as out:
        out.write(struct.pack("<III", 0x46546C67, 2, total))
        out.write(struct.pack("<II", len(json_chunk), 0x4E4F534A))
        out.write(json_chunk)
        out.write(struct.pack("<II", len(blob), 0x004E4942))
        out.write(blob)


def main(path):
    line = centre_line()
    stroke_positions, stroke_normals, stroke_faces = tube(line, STROKE_RADIUS)
    dot_positions, dot_normals, dot_faces = sphere(np.array([0.0, DOT_RADIUS, 0.0]), DOT_RADIUS)

    positions = np.concatenate([stroke_positions, dot_positions])
    normals = np.concatenate([stroke_normals, dot_normals])
    faces = np.concatenate([stroke_faces, dot_faces + len(stroke_positions)])

    write_glb(path, positions, normals, faces)
    low, high = positions.min(axis=0), positions.max(axis=0)
    print(f"wrote {path}")
    print(f"  vertices {len(positions)}, triangles {len(faces)}")
    print(f"  width  {high[0] - low[0]:.3f} m")
    print(f"  height {high[1] - low[1]:.3f} m")
    print(f"  depth  {high[2] - low[2]:.3f} m")
    print(f"  base sits at y={low[1]:.3f}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "app/src/main/assets/question_mark.glb")
