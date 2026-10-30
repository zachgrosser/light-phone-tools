# Collaborating

## Getting access

The repo owner adds people under **Settings → Collaborators → Add people** on GitHub. The invite email expires after a week. Collaborators can push branches, open pull requests and manage issues, but can't change settings.

Once you've accepted, install Android Studio, git and the [GitHub CLI](https://cli.github.com/), then:

```bash
gh auth login
```

```bash
gh repo clone zachgrosser/light-phone-tools
```

```bash
cd light-phone-tools && git remote add upstream https://github.com/lightphone/light-sdk.git
```

Then follow [DEVELOPING.md](DEVELOPING.md).

## Day to day

Nobody commits to `main`. Make a branch, push it, open a pull request, and get the other person to look at it.

```bash
git checkout main && git pull
```

```bash
git checkout -b add-timer-screen
```

Commit as you go, then push and open the pull request:

```bash
git push -u origin add-timer-screen
```

```bash
gh pr create --fill
```

Before opening one, run the tests and check your screens in `WikiPreviews.kt` at both sizes. GitHub then runs our Tool check and Light's PR Check.

To try someone else's pull request locally, use `gh pr checkout <number>`.

Once it's approved and green, use **Squash and merge** and delete the branch. The exception is a pull request that brings in Light's SDK: that one gets **Create a merge commit** (see [DEVELOPING.md](DEVELOPING.md#updating-lights-sdk)).

## Habits

- Pull `main` before starting something new.
- Keep pull requests small, one thing each.
- Use GitHub issues as the to-do list and assign yourself before starting.
- Leave Light's files alone (everything outside `tool/`, `scripts/` and our docs) so their updates merge cleanly. SDK suggestions go to [lightphone/light-sdk](https://github.com/lightphone/light-sdk).
- Never commit keys or passwords. `local.properties` and `signer/keys/` are already ignored.

## Careful areas

- `tool/src/main/kotlin/com/zachgrosser/wiki/Attribution.kt` and the About text: Wikipedia's license and Wikimedia's policies decide this wording. [tool/README.md](tool/README.md#attribution-and-wikipedias-rules) lists the sources.
- `tool/lighttool.toml`: the `id` can never change now that Wiki is submitted, and `serverPackage` has to stay `com.lightos`.
