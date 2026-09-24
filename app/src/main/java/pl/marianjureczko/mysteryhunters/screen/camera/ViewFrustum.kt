/*
 * Copyright (C) 2026 Marian Jureczko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package pl.marianjureczko.mysteryhunters.screen.camera

/**
 * Answers whether a place in the world would land on the screen, by pushing it through the very
 * matrices the scene is about to draw with.
 *
 * The arithmetic is written out here rather than handed to android.opengl.Matrix so that it can be
 * tested off a phone: the one thing that must not go wrong is refusing a hunter their point while
 * the mark stands in plain view in front of them, and that is a mistake of signs and conventions,
 * which is exactly what a test is good at catching.
 *
 * The scratch arrays are fields because this is asked once for every frame that is drawn.
 */
class ViewFrustum {

    private val viewProjection = FloatArray(MATRIX_SIZE)
    private val onScreen = FloatArray(VECTOR_SIZE)

    /** Where in the picture the last held place landed, each from -1 to 1. Only read after a true. */
    var pictureX: Float = 0f
        private set

    var pictureY: Float = 0f
        private set

    /**
     * @param projection the projection matrix of the camera, column major, as OpenGL and ARCore
     *        hand it over.
     * @param view the view matrix of the camera, in the same layout.
     * @return true when the place is in front of the camera and within the picture.
     */
    fun holds(projection: FloatArray, view: FloatArray, x: Float, y: Float, z: Float): Boolean {
        multiply(projection, view, viewProjection)
        transform(viewProjection, x, y, z, onScreen)
        val depth = onScreen[3]
        if (depth <= 0f) {
            // Behind the camera, where dividing through would fold it back onto the screen.
            return false
        }
        pictureX = onScreen[0] / depth
        pictureY = onScreen[1] / depth
        return pictureX in -1f..1f && pictureY in -1f..1f
    }

    private fun multiply(left: FloatArray, right: FloatArray, into: FloatArray) {
        for (column in 0 until 4) {
            for (row in 0 until 4) {
                var sum = 0f
                for (step in 0 until 4) {
                    sum += left[step * 4 + row] * right[column * 4 + step]
                }
                into[column * 4 + row] = sum
            }
        }
    }

    private fun transform(matrix: FloatArray, x: Float, y: Float, z: Float, into: FloatArray) {
        for (row in 0 until 4) {
            into[row] = matrix[row] * x + matrix[4 + row] * y + matrix[8 + row] * z + matrix[12 + row]
        }
    }

    companion object {
        private const val MATRIX_SIZE = 16
        private const val VECTOR_SIZE = 4
    }
}
