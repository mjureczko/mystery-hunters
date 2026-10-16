---
name: flow-plan
description: Leads plan creation and refinement in small, confirmable steps that keep the user in flow. Use when the user asks to plan, design, or architect a change before it is implemented, or to review or revise an existing plan.
---

# Flow on Planning

Flow is the state of full immersion in an activity: focus, involvement, and enjoyment. It arises from immediate feedback, a perceived capability to meet the task demands, and clear goals. Organize every planning session so the user gets all three: small steps, a visible impact after each answer, challenge matched to the user's demonstrated expertise, and explicit confirmation before anything advances.

This skill governs planning only: produce and maintain plan files, never implement the plan itself.

## Artifacts

- **Plan file:** `docs/plans/<YYYY-MM-DD>-<topic-slug>.md`, starting from [templates/plan.md](templates/plan.md). It is the single source of truth; chat quotes are for convenience only.
- **Detail files:** `docs/plans/<topic-slug>-*.md`, next to the plan file — rationale, rejected alternatives, and research that would bloat the plan.
- Keep the plan compact: one line per decision; rationale lives in detail files and is linked from the Decisions section.

## Procedure

Follow these steps in order. 
Do not start the next step until the user has explicitly confirmed it. 
Silence is not confirmation.

1. **Problem statement.**
State the issue to address together with the user. 
Abstract wording is fine at this stage — concretization comes later.
*Exit:* the problem statement is written in the plan file and the user confirms it.

2. **Context clarification.**
Collect the background needed to position the problem: the user's intentions, relevant sources (docs, links, file paths), and explicit facts. 
Ask for sources rather than assuming them.
*Exit:* the Context section is written and the user confirms nothing essential is missing.

3. **Big picture presentation.**
Present the big picture of the plan — at this stage mainly the problem together with the clarified context, without elements of the solution. 
Prefer a diagrams and images over plain text, omit low-relevance details, and confirm with the user that you agree on what is relevant. 
Expect and welcome clarification questions and corrections here.
*Exit:* the user explicitly confirms shared understanding (e.g., answers "confirmed").

4. **Solution design.**
The design is the central part of the plan.
Start by asking the user if they want to provide an initial design for the solution.
If not, create the solution gradually through asking user.

Proceed in small steps.
Ask single question and verify with user answer impact.
Then next step.
 
For each step, state the goal first (what you are clarifying or deciding, and why), then ask exactly one question — offering options whenever a choice exists.
After each answer, show how it changed the plan and get the user's approval that the change matches their intentions.
If an answer invalidates the Problem statement, Context, or Big picture, update that section, show the change to the user, and re-confirm it. 
Log every confirmed change in the Decisions section.
*Exit:* the design is complete and the user approves it.

During the whole procedure, park requests that fall outside the current plan in the plan's "Out of scope" section instead of following them — mention them to the user, but do not let them derail the session.

Keep the plan concise, without irrelevant details, rejected alternatives, or explanations of the paths leading to conclusions.
The details shall be stored in separate files, next to the plan file.

After each step, report progress: what has been done and what the next subgoal is.


## Rules

1. The plan is an md file by default (start from the template); use another format or a web page if the user asks for it.
2. Make it possible for the user to validate your claims: record the source or the line of reasoning in the plan so the user can verify and, if needed, reject them.
3. Commit plan-related files after each user-confirmed checkpoint. 
Amend only commits that have not been pushed; otherwise create a new commit.
4. Ask one question at a time; when several are needed, order them so the cognitive load increases gradually.
5. Always be prepared for clarification questions and correction requests.
6. Report progress at every step boundary, preferably as "what is done → what is the next subgoal".
7. Use diagrams and images whenever suitable (you may suggest that the user use a web page in place of md files). 
The documents you create shall be compact, and plain text is not the most concise form.
8. All artifacts (designs, decisions) shall be persisted in files. 
You can quote them in the chat for convenience, but there shall always be a file that is the source of trut