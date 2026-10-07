import argparse
import subprocess
import time


parser = argparse.ArgumentParser(description="Verify minified Santoro startup on a connected device.")
parser.add_argument("--adb", default="adb")
parser.add_argument("--serial", required=True)
args = parser.parse_args()
package = "com.asensiodev.santoro"


def adb(*command):
    return subprocess.run(
        [args.adb, "-s", args.serial, *command],
        capture_output=True,
        text=True,
        timeout=30,
    )


details = adb("shell", "dumpsys", "package", package)
if details.returncode or "versionName=" not in details.stdout:
    raise SystemExit("FAIL: Install the Release APK before running this check.")
if "DEBUGGABLE" in details.stdout:
    raise SystemExit("FAIL: This check requires the non-debuggable Release app.")

timestamp = adb("shell", "date", "'+%m-%d %H:%M:%S.000'")
if timestamp.returncode or not timestamp.stdout.strip():
    raise SystemExit("FAIL: Could not establish the device's crash-log timestamp.")

adb("shell", "am", "force-stop", package)
launch = adb("shell", "am", "start", "-W", "-n", f"{package}/.MainActivity")
if launch.returncode or "Error:" in launch.stdout + launch.stderr:
    raise SystemExit("FAIL: The Release activity could not start.")

for _ in range(5):
    time.sleep(1)
    crashes = adb("logcat", "-b", "crash", "-d", "-T", timestamp.stdout.strip())
    if crashes.returncode:
        raise SystemExit("FAIL: Could not inspect the device's crash buffer.")
    if f"Process: {package}," in crashes.stdout:
        raise SystemExit("FAIL: The Release app reported a startup crash.")
    process = adb("shell", "pidof", package)
    if process.returncode or not process.stdout.strip():
        raise SystemExit("FAIL: The Release app exited during startup.")

activity = adb("shell", "dumpsys", "activity", "activities")
resumed = [line for line in activity.stdout.splitlines() if "ResumedActivity" in line]
if activity.returncode or not any(package in line for line in resumed):
    raise SystemExit("FAIL: The Release app did not reach a resumed activity.")

print("PASS: The Release app stayed alive and reached a resumed activity.")
