---
name: flow-code
description: Drives implementation in small, feedback-rich steps that keep the user in flow. Use when the user asks to use it. 
  Can be used to implement, build, fix, refactor, or debug code, or to execute an approved plan.
---

# Flow on Coding

Flow is the state of full immersion in an activity: focus, involvement, and enjoyment.
It arises from immediate feedback, a perceived capability to meet the task demands, and clear goals — and in coding the feedback is mechanical: compilation, tests and visible diffs.
Organize every implementation session so the user gets all three: one small change at a time, validation after each change, challenge matched to the user's demonstrated expertise, and explicit confirmation before anything ambiguous or scope-expanding.

This skill governs implementation: write and validate code.
If a confirmed plan file exists, follow it and update it — do not silently re-plan.
Treat every deviation from the plan as a decision to confirm with the user.

## Artifacts

- **Plan or feature context:** follow the confirmed plan. 
Mark implemented steps as done and record deviations or new decisions in its Decisions section.
When the context-anchoring skill is available, use it to work with the feature context file.
The file may replace the plan or be an addition to it.
- **Big-picture:** a file showing the big picture of the desired solution.
Preferred format is md - diagrams and images are welcome.
- **TODO list:** kept in the big-picture file, directly below the big picture. 
New challenges and subtasks discovered during the session are written here instead of being started at once; the next subtask is chosen from this list when the current one is done; finished items are crossed off, not deleted.
Shall be used as the TODO list described by Beck in "TDD by example".
- **Code changes:** keep every step small, self-contained, and reviewable. 
The working tree is the source of truth; the diff is what the user sees.
- **Validation output:** validation belongs to the user.
Do not assume you are responsible for executing tests — the user may run them and not even inform you about the result.
If you need a result to proceed, ask for it explicitly.

## Procedure

Pair programming with ping-pong handoffs: the test-writer writes one small test, the implementer makes it pass. 
Follow the steps in order. 
The user may ask to swap roles at any time.

1. **Big picture of the solution.**
Before any pairing starts, present the big picture of the solution: the main pieces, how they fit together, and how the behavior will be tested. 
It must be known and agreed, but it may lack details — those are settled during pairing. 
Form it from the relevant code and, when present, the plan file; write the big picture into the big-picture file, and directly below it open a TODO list — seeded with any subtasks already visible; more will be discovered as the work proceeds.
*Exit criteria:* the user explicitly confirms agreement with the big picture. Pairing does not start before this.

2. **Role selection.**
Ask the user to choose the roles for this session, offering the options through the follow-up question tool:
- the agent writes the small tests, the user writes the production code;
- the user writes the small tests, the agent writes the production code.
*Exit:* roles are chosen.

3. **Pair programming loop.**
Repeat until the agreed big picture is fully implemented - note that the big picture may be altered during the session:
- **Choose the current subtask.** Ask user to pick the most suitable as next step item from the TODO list — offer the options through the follow-up question tool.
- **Test turn.** The test-writer states the intention behind the coming change (which behavior of the big picture this test pins down, and why), then writes one small test. 
The test is expected to be red.
- **Implementation turn.** The implementer states the intention behind the coming change (what it does and why), then makes the smallest change expected to turn the test green.
- **Discoveries.** When a turn surfaces a new challenge or subtask, write it on the TODO list and return to the current subtask — never start it while another subtask is in progress.
Consult the new items with user.
- **Subtask done.** When the current subtask is implemented and its test is green, cross it off the TODO list (mark it `- [x]`, keep the entry).
- When the user holds a turn, the other side inspects it and, when possible, runs it — reporting whatever is observed as immediate feedback. Help, but do not take over the user's turn unless asked.
- After each turn, report progress: what is done, what the next subgoal is.
- If reality disagrees with the big picture or plan, pause the loop, agree on the update with the user, edit the big-picture file or plan, and continue.
- User may act without explaining intention.
In such case you need to do your best to guess the intentions based on the provided changes.
Confirm with the user that your guess is correct.
When the guessing is not reliable, ask the user explicitly.
- Do a git commit before altering code, so when you hand control over to the user, they can view uncommitted changes and verify what you have done — to make it work smoothly, git add every file you create, without committing it.
When the last commit is not pushed, your git commit shall amend the last commit.

## Rules

1. One test, one behavior, one small implementation step at a time; one question at a time — with a small set of options whenever a choice exists, ordered broad to specific, easy to hard.
2. State your intention before every change you make — what it does and why — whether you are writing a test or production code.
3. Follow the existing codebase conventions — naming, structure, imports, test framework and style — matching the surrounding code over personal preference.
4. Keep changes inside the agreed big picture: no unrequested refactors or opportunistic improvements. 
Discovered subtasks, opertunities and side requests go on the TODO list — never jump to them mid-subtask.
5. Stay prepared for questions and correction requests — a correction may reopen the big picture: update it, show the change, and re-confirm.
