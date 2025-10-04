"""Build / SDK / Emulator automation script for NotePad project (Windows focused).

Usage examples (PowerShell):
  python build.py --prepare --build
  python build.py --build --emulator
  python build.py --emulator --no-build
  python build.py --clean

What it does:
    1. Detects Android SDK at default path or ANDROID_HOME.
    2. Locates cmdline-tools (tries 'latest', or first subfolder).
    3. Ensures required packages via sdkmanager (platforms;android-34, build-tools;34.0.0, platform-tools, emulator, system image when requested).
    4. Ensures Gradle wrapper (downloads Gradle 8.5 if missing, generates wrapper).
    5. Builds the project (assembleDebug).
    6. Ensures an emulator AVD exists; creates if missing.
    7. Optionally launches emulator.
    8. (Planned) Can auto-install and launch APK (flag to be added).

Notes:
  - Network required for first run (SDK components / Gradle distribution).
  - Simplified; not a replacement for full CI tooling.
  - On first sdkmanager license prompts this script attempts automatic acceptance.
"""

from __future__ import annotations
import argparse
import os
import shutil
import subprocess
import sys
import tempfile
import time
import zipfile
from pathlib import Path
from typing import List
from urllib.request import urlretrieve

DEFAULT_SDK = Path(r"C:\Users\purple\Desktop\Dev\Java\Android")
GRADLE_VERSION = "8.5"
GRADLE_DIST = f"gradle-{GRADLE_VERSION}-bin.zip"
GRADLE_URL = f"https://services.gradle.org/distributions/{GRADLE_DIST}"
SYSTEM_IMAGE = "system-images;android-34;google_apis;x86_64"
HYPERVISOR_PACKAGE = "extras;google;Android_Emulator_Hypervisor_Driver"
REQUIRED_PACKAGES = [
    "platform-tools",
    "platforms;android-34",
    "build-tools;34.0.0",
    "emulator",
]

# Candidate command-line tools (Windows) download URLs (newest first). Will try sequentially.
CMDLINE_TOOLS_CANDIDATES = [
    # Format: (description, url)
    ("11076708", "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"),
    ("10406996", "https://dl.google.com/android/repository/commandlinetools-win-10406996_latest.zip"),
]

ANSI = {
    'green': '\x1b[32m',
    'red': '\x1b[31m',
    'yellow': '\x1b[33m',
    'blue': '\x1b[34m',
    'reset': '\x1b[0m'
}

STREAM_MODE = False  # toggled by --stream flag


def color(msg: str, c: str) -> str:
    if not sys.stdout.isatty():
        return msg
    return f"{ANSI.get(c, '')}{msg}{ANSI['reset']}"


def run(cmd: List[str], env=None, check=True, shell=False):
    print(color(f"$ {' '.join(cmd)}", 'blue'))
    # If streaming is enabled and this is sdkmanager (long-running), stream output live.
    is_sdkmanager = any('sdkmanager' in part for part in cmd)
    stream = STREAM_MODE and is_sdkmanager and '--list' not in cmd and '--licenses' not in cmd
    if stream:
        proc = subprocess.run(cmd, env=env, text=True, shell=shell)
    else:
        proc = subprocess.run(cmd, env=env, text=True, capture_output=True, shell=shell)
        if proc.stdout:
            print(proc.stdout.strip())
        if proc.stderr:
            print(proc.stderr.strip())
    if check and proc.returncode != 0:
        raise SystemExit(f"Command failed: {' '.join(cmd)}")
    return proc


def detect_sdk() -> Path:
    env_home = os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT')
    if env_home:
        p = Path(env_home)
        if p.exists():
            print(color(f"Using SDK from environment: {p}", 'green'))
            return p
    if DEFAULT_SDK.exists():
        print(color(f"Using default SDK path: {DEFAULT_SDK}", 'green'))
        return DEFAULT_SDK
    raise SystemExit("Android SDK not found. Install cmdline-tools at default path or set ANDROID_HOME.")


def find_cmdline_tools(sdk: Path) -> Path:
    ct_root = sdk / 'cmdline-tools'
    if not ct_root.exists():
        raise SystemExit("cmdline-tools directory missing.")
    # Support flat layout: sdk/cmdline-tools/bin
    direct_bin = ct_root / 'bin'
    if direct_bin.exists():
        # sdkmanager expects <sdk>/cmdline-tools/<label>/bin; create 'latest' if absent
        latest = ct_root / 'latest'
        if not (latest / 'bin').exists():
            try:
                latest.mkdir(exist_ok=True)
                # Copy only if bin not present
                if not (latest / 'bin').exists():
                    shutil.copytree(direct_bin.parent, latest, dirs_exist_ok=True)
            except Exception as e:
                print(color(f"Warning: could not synthesize 'latest' layout: {e}", 'red'))
        return (latest / 'bin') if (latest / 'bin').exists() else direct_bin
    # Prefer 'latest'
    latest = ct_root / 'latest'
    if (latest / 'bin').exists():
        return latest / 'bin'
    # Fallback: pick first directory containing bin
    for child in ct_root.iterdir():
        if (child / 'bin').exists():
            return child / 'bin'
    raise SystemExit("No cmdline-tools bin folder found.")


def bootstrap_cmdline_tools(sdk: Path):
    ct_root = sdk / 'cmdline-tools'
    latest_dir = ct_root / 'latest'
    bin_dir = latest_dir / 'bin'
    if bin_dir.exists():
        return bin_dir
    ct_root.mkdir(parents=True, exist_ok=True)
    # Try downloads
    tmp = Path(tempfile.mkdtemp())
    for label, url in CMDLINE_TOOLS_CANDIDATES:
        zip_path = tmp / f"cmdline-tools-{label}.zip"
        try:
            print(color(f"Downloading Android cmdline-tools {label}...", 'yellow'))
            urlretrieve(url, zip_path)
            extract_dir = tmp / f"extract-{label}"
            with zipfile.ZipFile(zip_path, 'r') as zf:
                zf.extractall(extract_dir)
            # Zip contains a top-level 'cmdline-tools' directory; move its contents under latest/
            inner = extract_dir / 'cmdline-tools'
            if not inner.exists():
                print(color("Downloaded archive missing expected 'cmdline-tools' folder.", 'red'))
                continue
            latest_dir.mkdir(exist_ok=True)
            # Move children (bin, lib, etc.)
            for child in inner.iterdir():
                dest = latest_dir / child.name
                if dest.exists():
                    continue
                if child.is_dir():
                    shutil.copytree(child, dest)
                else:
                    shutil.copy2(child, dest)
            if (latest_dir / 'bin').exists():
                print(color("cmdline-tools bootstrap complete.", 'green'))
                return latest_dir / 'bin'
        except Exception as e:
            print(color(f"Failed downloading {label}: {e}", 'red'))
    raise SystemExit("Failed to bootstrap Android cmdline-tools automatically. Install manually from Android developer site.")


def sdkmanager_bin(bin_dir: Path) -> Path:
    # On Windows there is sdkmanager.bat
    bat = bin_dir / 'sdkmanager.bat'
    return bat if bat.exists() else (bin_dir / 'sdkmanager')


def avdmanager_bin(bin_dir: Path) -> Path:
    bat = bin_dir / 'avdmanager.bat'
    return bat if bat.exists() else (bin_dir / 'avdmanager')


def ensure_packages(bin_dir: Path, sdk: Path, include_system_image: bool, accept_licenses: bool = True):
    sm = str(sdkmanager_bin(bin_dir))
    pkgs = REQUIRED_PACKAGES.copy()
    if include_system_image:
        pkgs.append(SYSTEM_IMAGE)
    # Hypervisor package is optional; we add only when explicitly requested elsewhere.
    installed = list_installed_packages(bin_dir, sdk)
    missing = [p for p in pkgs if p not in installed]
    if not missing:
        print(color("All required SDK packages already present (skipping install).", 'green'))
        summarize_packages(bin_dir, include_system_image)
        return
    print(color(f"Missing packages: {', '.join(missing)}", 'yellow'))
    attempts = 0
    last_err = None
    while attempts < 2:
        attempts += 1
        try:
            run([sm, f'--sdk_root={sdk}', *missing])
            last_err = None
            break
        except SystemExit as e:
            last_err = e
            print(color(f"Install attempt {attempts} failed: {e}.", 'red'))
            if attempts < 2:
                print(color("Retrying once more...", 'yellow'))
                time.sleep(2)
    if last_err:
        raise last_err
    if accept_licenses:
        proc = subprocess.Popen([sm, f'--sdk_root={sdk}', '--licenses'], stdin=subprocess.PIPE, text=True)
        try:
            proc.communicate(input='y\n' * 50, timeout=120)
        except subprocess.TimeoutExpired:
            proc.kill()
            print(color("License acceptance timed out; rerun manually if needed.", 'red'))
    summarize_packages(bin_dir, include_system_image)


def list_installed_packages(bin_dir: Path, sdk: Path) -> set:
    sm = str(sdkmanager_bin(bin_dir))
    proc = subprocess.run([sm, f'--sdk_root={sdk}', '--list'], text=True, capture_output=True)
    installed = set()
    if proc.stdout:
        lines = proc.stdout.splitlines()
        in_section = False
        for line in lines:
            if line.strip().startswith('Installed packages:'):
                in_section = True
                continue
            if in_section:
                if not line.strip():
                    break
                parts = line.split('|')
                if parts:
                    pkg = parts[0].strip()
                    if pkg:
                        installed.add(pkg)
    return installed


def summarize_packages(bin_dir: Path, include_system_image: bool):
    """Print a concise status of required SDK components."""
    required = set(REQUIRED_PACKAGES)
    if include_system_image:
        required.add(SYSTEM_IMAGE)
    sm = str(sdkmanager_bin(bin_dir))
    proc = subprocess.run([sm, '--list'], text=True, capture_output=True)
    installed = set()
    lines = (proc.stdout or '').splitlines()
    in_installed = False
    for line in lines:
        if line.strip().startswith('Installed packages:'):
            in_installed = True
            continue
        if in_installed:
            if not line.strip():
                break
            parts = line.split('|')
            if parts:
                pkg = parts[0].strip()
                if pkg:
                    installed.add(pkg)
    missing = [p for p in required if p not in installed]
    print(color('--- SDK Summary ---', 'yellow'))
    for p in sorted(required):
        ok = p in installed
        print(color(f"{p} : {'OK' if ok else 'MISSING'}", 'green' if ok else 'red'))
    if missing:
        print(color('Still missing: ' + ', '.join(missing), 'red'))
    else:
        print(color('All required packages installed.', 'green'))


def verify_file_structure(sdk: Path, include_system_image: bool) -> bool:
    checks = [
        (sdk / 'platform-tools' / 'adb.exe', 'platform-tools adb.exe'),
        (sdk / 'build-tools' / '34.0.0' / 'aapt2.exe', 'build-tools 34.0.0 aapt2.exe'),
        (sdk / 'platforms' / 'android-34' / 'android.jar', 'platforms android-34 android.jar'),
        (sdk / 'emulator' / 'emulator.exe', 'emulator binary'),
    ]
    if include_system_image:
        checks.append((sdk / 'system-images' / 'android-34' / 'google_apis' / 'x86_64' / 'package.xml', 'system image (package.xml)'))
    all_ok = True
    print(color('--- File Verification ---', 'yellow'))
    for path, label in checks:
        if path.exists():
            print(color(f"OK  - {label}", 'green'))
        else:
            print(color(f"MISS - {label} (expected at {path})", 'red'))
            all_ok = False
    return all_ok


def ensure_gradle_wrapper(project_root: Path):
    wrapper = project_root / 'gradlew.bat'
    if wrapper.exists():
        print(color("Gradle wrapper already present.", 'green'))
        return
    dist_dir = project_root / '.gradle-dist'
    dist_dir.mkdir(exist_ok=True)
    zip_path = dist_dir / GRADLE_DIST
    if not zip_path.exists():
        print(color(f"Downloading Gradle {GRADLE_VERSION}...", 'yellow'))
        urlretrieve(GRADLE_URL, zip_path)
    extract_dir = dist_dir / f'gradle-{GRADLE_VERSION}'
    if not extract_dir.exists():
        print(color("Extracting Gradle distribution...", 'yellow'))
        with zipfile.ZipFile(zip_path, 'r') as zf:
            zf.extractall(dist_dir)
    gradle_bin = extract_dir / 'bin' / 'gradle.bat'
    if not gradle_bin.exists():
        raise SystemExit("Downloaded Gradle distribution invalid (gradle.bat not found).")
    print(color("Generating wrapper...", 'yellow'))
    run([str(gradle_bin), 'wrapper', f'--gradle-version={GRADLE_VERSION}'], check=True)
    print(color("Wrapper generated.", 'green'))


def build(project_root: Path, task: str = 'assembleDebug'):
    wrapper = project_root / 'gradlew.bat'
    if not wrapper.exists():
        raise SystemExit("Gradle wrapper missing (call ensure_gradle_wrapper first).")
    run([str(wrapper), task])


def list_avds() -> List[str]:
    emulator = shutil.which('emulator') or shutil.which('emulator.exe')
    if not emulator:
        return []
    proc = subprocess.run([emulator, '-list-avds'], text=True, capture_output=True)
    if proc.returncode != 0:
        return []
    return [l.strip() for l in proc.stdout.splitlines() if l.strip()]


def ensure_avd(bin_dir: Path, sdk: Path, avd_name: str = 'TestApi34'):
    if avd_name in list_avds():
        print(color(f"AVD '{avd_name}' already exists.", 'green'))
        return avd_name
    print(color(f"Creating AVD '{avd_name}'...", 'yellow'))
    avdm = str(avdmanager_bin(bin_dir))
    # Accept default hardware profile
    cmd = [avdm, 'create', 'avd', '--name', avd_name, '--package', SYSTEM_IMAGE, '--device', 'pixel']
    proc = subprocess.Popen(cmd, stdin=subprocess.PIPE, text=True)
    try:
        proc.communicate(input='no\n', timeout=120)  # Choose no custom hardware profile
    except subprocess.TimeoutExpired:
        proc.kill()
        raise SystemExit("AVD creation timed out.")
    if proc.returncode != 0:
        raise SystemExit("Failed to create AVD.")
    return avd_name


def launch_emulator(avd_name: str):
    emulator = shutil.which('emulator') or shutil.which('emulator.exe')
    if not emulator:
        raise SystemExit("emulator binary not found in PATH. Ensure platform-tools & emulator installed.")
    print(color(f"Launching emulator {avd_name}...", 'yellow'))
    subprocess.Popen([emulator, '-avd', avd_name])  # background
    # Wait a bit to let it start (optional)
    time.sleep(5)
    print(color("Emulator launch initiated.", 'green'))


def wait_for_emulator_boot(adb: Path, timeout: int = 300):
    """Wait until emulator reports sys.boot_completed=1 or timeout seconds elapse."""
    start = time.time()
    print(color("Waiting for emulator to report boot completed...", 'yellow'))
    subprocess.run([str(adb), 'wait-for-device'], capture_output=True)
    while time.time() - start < timeout:
        proc = subprocess.run([str(adb), 'shell', 'getprop', 'sys.boot_completed'], text=True, capture_output=True)
        if proc.stdout.strip() == '1':
            print(color("Emulator boot complete.", 'green'))
            return True
        time.sleep(2)
    print(color("Timed out waiting for emulator boot.", 'red'))
    return False


def clean(project_root: Path):
    build_dir = project_root / 'app' / 'build'
    if build_dir.exists():
        shutil.rmtree(build_dir)
        print(color("Removed build directory.", 'yellow'))


def parse_args():
    ap = argparse.ArgumentParser(description="Automate Android build & emulator setup.")
    ap.add_argument('--prepare', action='store_true', help='Install SDK packages & Gradle wrapper if missing')
    ap.add_argument('--build', action='store_true', help='Run assembleDebug build')
    ap.add_argument('--task', default='assembleDebug', help='Custom Gradle task (default assembleDebug)')
    ap.add_argument('--emulator', action='store_true', help='Launch (and create if needed) an emulator')
    ap.add_argument('--avd', default='TestApi34', help='AVD name to use/create')
    ap.add_argument('--install-apk', action='store_true', help='After emulator boot, install and launch debug APK')
    ap.add_argument('--no-system-image', action='store_true', help='Skip ensuring system image')
    ap.add_argument('--clean', action='store_true', help='Delete build artifacts')
    ap.add_argument('--no-build', action='store_true', help='Skip build even if --emulator given')
    ap.add_argument('--all', action='store_true', help='Equivalent to --prepare --build --emulator (full pipeline)')
    ap.add_argument('--interactive', action='store_true', help='Prompt for actions if no flags provided')
    ap.add_argument('-v', '--verbose', action='store_true', help='Verbose logging')
    ap.add_argument('--status', action='store_true', help='Show install status of required SDK components and exit')
    ap.add_argument('--auto', action='store_true', help='Auto ensure required packages then build (no emulator)')
    ap.add_argument('--strict-verify', action='store_true', help='Fail if post-install verification finds missing files')
    ap.add_argument('--stream', action='store_true', help='Stream sdkmanager output live (no capture)')
    ap.add_argument('--hypervisor', action='store_true', help='Install Android Emulator Hypervisor Driver (Windows) if available')
    return ap.parse_args()


def main():
    args = parse_args()
    global STREAM_MODE
    STREAM_MODE = args.stream
    project_root = Path(__file__).parent
    sdk = detect_sdk()
    try:
        bin_dir = find_cmdline_tools(sdk)
    except SystemExit as e:
        msg = str(e)
        if 'cmdline-tools directory missing' in msg or 'No cmdline-tools bin folder found' in msg:
            print(color("cmdline-tools not found; attempting automatic bootstrap...", 'yellow'))
            bin_dir = bootstrap_cmdline_tools(sdk)
        else:
            raise

    # Update PATH for current process so emulator / platform-tools are discoverable
    os.environ['ANDROID_HOME'] = str(sdk)
    os.environ['ANDROID_SDK_ROOT'] = str(sdk)
    add_paths = [str(bin_dir), str(sdk / 'platform-tools'), str(sdk / 'emulator')]
    os.environ['PATH'] = os.pathsep.join(add_paths + [os.environ.get('PATH', '')])

    if args.all:
        args.prepare = True
        # only build unless user adds --no-build
        if not args.no_build:
            args.build = True
        args.emulator = True

    if args.auto:
        args.prepare = True
        args.build = True

    if args.status and not any([args.prepare, args.build, args.emulator, args.clean, args.all, args.auto]):
        summarize_packages(bin_dir, include_system_image=not args.no_system_image)
        verify_file_structure(sdk, include_system_image=not args.no_system_image)
        return

    if args.status and not any([args.prepare, args.build, args.emulator, args.clean]):
        summarize_packages(bin_dir, include_system_image=not args.no_system_image)
        return

    if args.clean:
        clean(project_root)

    if args.prepare:
        ensure_packages(bin_dir, sdk, include_system_image=not args.no_system_image)
        ensure_gradle_wrapper(project_root)
        if args.strict_verify:
            ok = verify_file_structure(sdk, include_system_image=not args.no_system_image)
            if not ok:
                raise SystemExit('Verification failed: missing required files after prepare.')
        if args.hypervisor:
            # Attempt to install hypervisor driver package and run silent installer
            print(color('Attempting hypervisor driver installation...', 'yellow'))
            sm = str(sdkmanager_bin(bin_dir))
            try:
                run([sm, f'--sdk_root={sdk}', HYPERVISOR_PACKAGE], check=False)
            except Exception as e:
                print(color(f'Failed to request hypervisor package: {e}', 'red'))
            driver_dir = sdk / 'extras' / 'google' / 'Android_Emulator_Hypervisor_Driver'
            silent = driver_dir / 'silent_install.bat'
            if silent.exists():
                print(color('Running hypervisor silent installer...', 'yellow'))
                proc = subprocess.run([str(silent)], text=True, capture_output=True)
                if proc.returncode == 0:
                    print(color('Hypervisor driver installer completed (check output above for confirmation).', 'green'))
                else:
                    print(color('Hypervisor driver installer returned non-zero exit code.', 'red'))
                    if proc.stdout:
                        print(proc.stdout)
                    if proc.stderr:
                        print(proc.stderr)
            else:
                print(color('Hypervisor driver silent installer not found (package may not support this platform).', 'red'))

    if args.build and not args.no_build:
        ensure_gradle_wrapper(project_root)
        build(project_root, args.task)
        if args.strict_verify:
            ok = verify_file_structure(sdk, include_system_image=not args.no_system_image)
            if not ok:
                raise SystemExit('Verification failed after build.')

    if args.emulator:
        ensure_packages(bin_dir, sdk, include_system_image=True)
        avd = ensure_avd(bin_dir, sdk, args.avd)
        launch_emulator(avd)
        if getattr(args, 'install_apk', False):
            adb = sdk / 'platform-tools' / 'adb.exe'
            if not adb.exists():
                print(color('adb.exe not found; cannot auto-install APK.', 'red'))
            else:
                booted = wait_for_emulator_boot(adb)
                apk_path = Path(__file__).parent / 'app' / 'build' / 'outputs' / 'apk' / 'debug' / 'app-debug.apk'
                if not apk_path.exists():
                    print(color('Debug APK missing; building assembleDebug...', 'yellow'))
                    ensure_gradle_wrapper(project_root)
                    build(project_root, 'assembleDebug')
                if apk_path.exists() and booted:
                    print(color(f'Installing {apk_path.name} ...', 'yellow'))
                    subprocess.run([str(adb), 'install', '-r', str(apk_path)], text=True)
                    print(color('Launching app main activity...', 'yellow'))
                    subprocess.run([str(adb), 'shell', 'am', 'start', '-n', 'com.example.notepad/.MainActivity'], text=True)
                else:
                    print(color('Skipping install (APK missing or emulator not booted).', 'red'))

    if not any([args.prepare, args.build, args.emulator, args.clean, args.all, args.auto]):
        if args.interactive:
            print(color("Interactive mode:", 'yellow'))
            print("1) Prepare (SDK + wrapper)\n2) Build\n3) Build + Emulator\n4) All (prepare+build+emulator)\n5) Clean\n0) Exit")
            choice = input('Select option: ').strip()
            mapping = {
                '1': ['--prepare'],
                '2': ['--build'],
                '3': ['--build', '--emulator'],
                '4': ['--all'],
                '5': ['--clean']
            }
            if choice in mapping:
                print(color(f"Re-run: python build.py {' '.join(mapping[choice])}", 'blue'))
            else:
                print("Exit.")
        else:
            print(color("No flags provided. Running default auto pipeline (prepare + build).", 'yellow'))
            # Default pipeline
            try:
                ensure_packages(bin_dir, sdk, include_system_image=False)
                ensure_gradle_wrapper(project_root)
                build(project_root, 'assembleDebug')
                verify_file_structure(sdk, include_system_image=False)
                print(color("Default build complete (APK in app/build/outputs/apk/debug)", 'green'))
            except Exception as e:
                print(color(f"Default build failed: {e}", 'red'))
                print("Hint: run with --prepare --build for more explicit output")


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print(color("Interrupted.", 'red'))
