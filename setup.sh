#!/bin/bash

# Function to display usage instructions
usage() {
    echo "Usage: $0 [DEVELOP|RELEASE]"
    echo "  DEVELOP  - Sets up the environment for Android emulator (x86_64, arm64-v8a)"
    echo "  RELEASE  - Sets up the environment for real Android devices (armeabi-v7a, arm64-v8a)"
    echo "  --help   - Show this help message"
    exit 1
}

# Ensure an argument is provided
if [ $# -ne 1 ]; then
    echo "Error: Missing required argument."
    usage
fi

# Handle --help option
if [[ "$1" == "--help" ]]; then
    usage
fi

# Convert argument to uppercase for case-insensitive comparison
ARG=$(echo "$1" | tr '[:lower:]' '[:upper:]')

# Validate argument and set the correct Rust targets
case "$ARG" in
    DEVELOP)
        TARGETS=("x86_64-linux-android" "aarch64-linux-android")
        ;;
    RELEASE)
        TARGETS=("armv7-linux-androideabi" "aarch64-linux-android")
        ;;
    *)
        echo "Error: Unexpected argument '$1'. Expected DEVELOP or RELEASE."
        usage
        ;;
esac

echo "Setting up environment for $ARG mode with targets: ${TARGETS[*]}"

# Ensure Homebrew is up-to-date
echo "Updating Homebrew..."
brew update

# Install necessary dependencies
echo "Installing dependencies..."
brew install cmake pkg-config

# Check if rustup is installed
if ! command -v rustup &>/dev/null; then
    echo "rustup not found! Installing rustup..."
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
else
    echo "rustup is already installed"
fi

# Check if cargo-ndk is installed
if ! command -v cargo-ndk &>/dev/null; then
    echo "cargo-ndk not found! Installing cargo-ndk..."
    cargo install cargo-ndk
else
    echo "cargo-ndk is already installed"
fi

# Set the Rust toolchain to stable
echo "Setting up Rust toolchain..."
rustup default stable

# Add Android targets
for TARGET in "${TARGETS[@]}"; do
    echo "Adding Rust target: $TARGET..."
    rustup target add "$TARGET"
done

# Ensure we're in the correct directory
echo "Running setup from $(pwd)"

# Check if mazer/ exists; if not, clone it
if [ ! -d "mazer" ]; then
    echo "Cloning mazer repository..."
    git clone https://github.com/jmisabella/mazer.git mazer
else
    echo "Updating mazer submodule..."
    git -C mazer pull origin main
fi

# Navigate into mazer directory
cd mazer || { echo "Error: 'mazer' directory not found"; exit 1; }

# Remove old build artifacts
echo "Cleaning up old build artifacts..."
rm -rf target/

# Update dependencies
echo "Updating dependencies from crates.io..."
cargo update

# Ensure Cargo.toml is set for shared library output
echo "Ensuring crate-type is set to cdylib in Cargo.toml..."
if grep -q '^\[lib\]' Cargo.toml; then
    if ! grep -q 'crate-type = \["cdylib"\]' Cargo.toml; then
        sed -i '' '/^\[lib\]/a\
crate-type = ["cdylib"]
' Cargo.toml
        echo "Updated [lib] section in Cargo.toml to include crate-type cdylib."
    fi
else
    sed -i '' '1i\
[lib]\
crate-type = ["cdylib"]
' Cargo.toml
    echo "Added [lib] section to Cargo.toml with crate-type cdylib."
fi

# Build mazer library for Android targets using cargo-ndk
echo "Building mazer library for targets: ${TARGETS[*]}..."
for TARGET in "${TARGETS[@]}"; do
    case "$TARGET" in
        "x86_64-linux-android")
            ABI="x86_64"
            ;;
        "aarch64-linux-android")
            ABI="arm64-v8a"
            ;;
        "armv7-linux-androideabi")
            ABI="armeabi-v7a"
            ;;
        *)
            echo "Unknown target: $TARGET"
            continue
            ;;
    esac
    cargo ndk -t "$ABI" build --release
done

# Copy include/mazer.h to mazer-android/ for JNI integration
if [[ -f "include/mazer.h" ]]; then
    cp "include/mazer.h" ../mazer.h
    echo "File 'mazer.h' copied to mazer-android directory."
else
    echo "Error: 'mazer.h' does not exist in 'mazer/include/'."
fi

# Copy .so files to app/src/main/jniLibs/
echo "Copying .so files to Android project jniLibs..."
mkdir -p ../app/src/main/jniLibs
for TARGET in "${TARGETS[@]}"; do
    case "$TARGET" in
        "x86_64-linux-android")
            ABI="x86_64"
            ;;
        "aarch64-linux-android")
            ABI="arm64-v8a"
            ;;
        "armv7-linux-androideabi")
            ABI="armeabi-v7a"
            ;;
        *)
            echo "Unknown target: $TARGET"
            continue
            ;;
    esac
    mkdir -p "../app/src/main/jniLibs/$ABI"
    cp "target/$TARGET/release/libmazer.so" "../app/src/main/jniLibs/$ABI/libmazer.so"
    echo "Copied libmazer.so for $ABI to jniLibs/$ABI/"
done

# Navigate back to mazer-android directory
cd ..

# Print Rust and target information
echo "Rust setup completed. Current Rust version:"
rustc --version
echo "Targets added for Android:"
rustup target list --installed

# End of script
echo "Setup complete! You should now be ready to build the Android app with Rust integration."
