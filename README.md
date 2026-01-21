🛡️ QR Guard
Aplicație nativă Android pentru scanarea și analiza securizată a codurilor QR, dezvoltată în Kotlin și Jetpack Compose.

📋 Funcționalități Principale
Smart Scanner (CameraX): Scanare în timp real cu overlay grafic. Detectează automat: URL, vCard (Contacte), Wi-Fi, Text.

Securitate Activă (VirusTotal API): Verifică link-urile împotriva malware/phishing prin request-uri REST API.

Criptare AES: Modul de decriptare pentru coduri QRSAFE: protejate prin parolă.

Cyber Academy: Integrare date MITRE ATT&CK® și sistem de quiz-uri (Gamification) cu salvare scor în Firebase Firestore.

Istoric & Persistență: Salvare locală în Room Database cu indicatori vizuali de risc (Safe/Threat).

Simulare Atac: Folosește AlarmManager și BroadcastReceiver pentru notificări push programate.

🛠️ Stack Tehnologic & Arhitectură
Arhitectură: MVVM (Model-View-ViewModel), Clean Architecture.

UI: Jetpack Compose (Material Design 3).

Async: Kotlin Coroutines & Flow.

Networking: Retrofit 2 + Gson (REST API), Firebase Auth & Firestore.

Local Data: Room Database (SQLite).

Hardware: Android CameraX, Flashlight Control.
