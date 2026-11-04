# AI Model Asset Pack (`:features:ai:model_pack`)

This module is a **Google Play Asset Pack** configured for `on-demand` delivery. It is responsible for securely delivering the large AI model (e.g., Gemma 3 or Qwen) to the user's device without bloating the initial app install size.

Because the model files are typically 1.5 GB to 2 GB, **they are not checked into Git.** 

## How to get the Model File

To build this project and test the local AI feature, you need to place a valid `.litertlm` (or `.bin`) model file at:
`src/main/assets/model.litertlm`

You can obtain the model using one of the following methods:

### Method 1: Using the Official `litert-lm` Python Tool (Recommended)
Google provides an official Python CLI tool to easily authenticate, download, and test models locally before bundling them into your Android app.

1. Install the tool:
```bash
pip install -U litert-lm
```

2. Automatically download and run the file locally (this validates your Hugging Face token and tests the model):
```bash
litert-lm run --from-huggingface-repo=google/gemma-3n-E4B-it-litert-lm --prompt="Write me a poem"
```

3. Once downloaded, copy the cached model file into this asset pack:
```bash
cp ~/.cache/huggingface/hub/models--google--gemma-3n-E4B-it-litert-lm/snapshots/.../model.litertlm src/main/assets/model.litertlm
```
*(Note: Check the exact path in your huggingface cache).*

### Method 2: Using the provided Bash Script
If you prefer to download the file directly via `curl`, you can use the provided script:

```bash
cd features/ai/model_pack
chmod +x download_model.sh

# To download an ungated model (like Qwen2.5) for quick testing:
./download_model.sh

# To download a gated model (like Gemma 2 or 3), pass your Hugging Face token:
HF_TOKEN="hf_YOUR_TOKEN" ./download_model.sh
```

## Testing Play Asset Delivery

If you run the app directly from Android Studio via a USB cable, Android Studio will automatically inject this asset pack into your device via ADB. 

However, to test the actual background download UI and progress bar:
1. Generate an **Android App Bundle (.aab)** (`Build > Generate Signed Bundle / APK`).
2. Upload the `.aab` to the **Google Play Console** via **Internal App Sharing**.
3. Install the app via the provided Play Store link on your phone.
4. Open the app and click "Download AI" in the AI Configuration menu.
