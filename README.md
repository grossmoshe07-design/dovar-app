This project is a simple Android WebView wrapper for a single-domain site.

Key details:
- Package name / applicationId: mnhg.dovar
- App label: dovar shalom
- Startup URL: https://emailphone.free.nf/
- Offline behavior: Uses WebView cache and serves assets/offline.html when the device is offline.

How to include your logo:
- If you want the launcher icon included, upload your logo (preferably a transparent PNG). I will place it as:
  - app/src/main/res/mipmap-anydpi-v26/ic_launcher_foreground.png
  - and optionally generate mipmap-*/ic_launcher_foreground.png for other densities.
- To show the logo in the offline page, either embed a base64 data URI into assets/offline.html or place the file in assets/ and reference it from the HTML.
