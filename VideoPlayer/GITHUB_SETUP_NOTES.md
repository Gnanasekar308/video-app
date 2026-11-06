# GitHub Setup Notes

1. **Create or open a repo** – create a new repository on GitHub (or clone your existing one): `git clone <repo-url> && cd <repo>`
2. **Add the project** – copy this folder to `apps/videoplayer/` inside the repo.
3. **Commit** – `git add apps/videoplayer && git commit -m "Add VideoPlayer Android app scaffold"`
4. **Push** – `git push origin main`
5. **Optional later** – add a GitHub Actions workflow (e.g. `.github/workflows/android.yml`) to run `./gradlew assembleDebug` once you're ready to build. Note: add the Gradle wrapper (`gradle wrapper`) first.
