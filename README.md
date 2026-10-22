<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:0f2027,50:203a43,100:2c5364&height=220&section=header&text=Keep%20Safe&fontSize=64&fontColor=ffffff&animation=fadeIn&fontAlignY=38&desc=Your%20personal%20guard%20against%20fraud%20and%20phishing&descAlignY=60&descSize=20" alt="Keep Safe banner" width="100%"/>

# 🛡️ أمان — Keep Safe

**حارسك الشخصي ضد الاحتيال والتصيّد الإلكتروني**

<br/>

![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

[![Build](https://img.shields.io/github/actions/workflow/status/NULL200OK/keep-safe/build.yml?style=flat-square&logo=githubactions&logoColor=white&label=build)](https://github.com/NULL200OK/keep-safe/actions)
![Languages](https://img.shields.io/badge/languages-5-36D7B7?style=flat-square)
![Version](https://img.shields.io/badge/version-1.0.0-blue?style=flat-square)
[![Stars](https://img.shields.io/github/stars/NULL200OK/keep-safe?style=flat-square&logo=github&color=gold)](https://github.com/NULL200OK/keep-safe/stargazers)
[![Last commit](https://img.shields.io/github/last-commit/NULL200OK/keep-safe?style=flat-square&color=orange)](https://github.com/NULL200OK/keep-safe/commits/main)

**[✨ الميزات](#-الميزات) · [🧠 كيف يعمل](#-كيف-يعمل) · [🧱 التقنيات](#-التقنيات) · [🚀 البناء](#-البناء-والتشغيل) · [🗂️ الهيكل](#️-هيكل-المشروع) · [🤝 المساهمة](#-المساهمة)**

</div>

---

## 🌟 نظرة عامة

**أمان (Keep Safe)** تطبيق أندرويد متكامل لحمايتك من الاحتيال والتصيّد الإلكتروني. يفحص الروابط والرسائل والمكالمات وملفات APK وشبكات الواي فاي، ويضيف حماية للأطفال وقفلًا بالبصمة أو الرمز السري — بواجهة عصرية مبنية بـ **Jetpack Compose** وتدعم **5 لغات**.

> 🔒 **الخصوصية أولًا:** الفحص يتم على جهازك، بدون حسابات.

<table>
<tr>
<td align="center" width="25%">🔗<br/><b>روابط</b><br/><sub>Punycode / Homograph</sub></td>
<td align="center" width="25%">💬<br/><b>رسائل</b><br/><sub>SMS والبريد</sub></td>
<td align="center" width="25%">📞<br/><b>مكالمات</b><br/><sub>كشف الاحتيال والمزعجين</sub></td>
<td align="center" width="25%">📦<br/><b>APK</b><br/><sub>تحليل التطبيقات</sub></td>
</tr>
</table>

---

## ✨ الميزات

| | الميزة | الوصف |
|---|---|---|
| 🔗 | **فحص الروابط** | كشف هجمات Punycode وHomograph والنطاقات المنتحلة، مع قاعدة تهديدات قابلة للتحديث |
| 📦 | **تحليل APK** | فحص الملفات المشبوهة قبل تثبيتها |
| 💬 | **فحص SMS والبريد** | اكتشاف رسائل التصيّد مع إشعارات ذكية |
| 📞 | **فحص المكالمات** | تنبيه للأرقام الاحتيالية والمزعجة |
| 📶 | **ماسح Wi-Fi** | تقييم أمان الشبكة المتصل بها |
| 🔳 | **ماسح QR** | تحقق من وجهة الرمز قبل فتحه |
| 🌐 | **VPN محلي** | فلترة حركة المرور على الجهاز |
| 👨‍👩‍👧 | **حماية الأطفال** | فلتر DNS للعائلة وإعدادات رقابة أبوية |
| 🔑 | **فحص التسريبات** | تحقق من تعرّض بياناتك للاختراق |
| 🛠️ | **فحص أمان الجهاز** | مراجعة إعدادات الجهاز الحساسة |
| 🧬 | **القفل** | بصمة الإصبع أو رمز PIN |
| 📊 | **إحصائيات وتقارير** | سجل الفحوص، ملخص يومي وتقرير أسبوعي، تصدير **PDF / CSV** |
| 🌍 | **5 لغات** | العربية · English · Français · Español · Türkçe |

<details>
<summary><b>🔎 تفاصيل أكثر (اضغط للتوسيع)</b></summary>

<br/>

- **Punycode / Homograph:** يكشف النطاقات ذات الأحرف المتشابهة المنتحلة لمواقع معروفة.
- **تحديث التهديدات:** عامل خلفي يحدّث قاعدة التهديدات دوريًا.
- **التقارير:** ملخص يومي وتقرير أسبوعي عبر WorkManager.
- **الإقلاع:** استعادة الحماية تلقائيًا بعد إعادة تشغيل الجهاز.

</details>

---

## 🧠 كيف يعمل

```mermaid
flowchart LR
    A([📥 رابط / رسالة / مكالمة / APK]) --> B{🔍 المحلِّل}
    B --> C[قاعدة التهديدات]
    B --> D[قواعد الأنماط]
    B --> E[فحص النطاق]
    C & D & E --> F{⚖️ تقييم الخطورة}
    F -->|آمن| G[✅ سليم]
    F -->|مشبوه| H[⚠️ تحذير]
    F -->|خطير| I[⛔ تنبيه]
    G & H & I --> J[(🗄️ السجل · Room)]
    J --> K[📊 إحصائيات · PDF/CSV]
```

---

## 🧱 التقنيات

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material3](https://img.shields.io/badge/Material_3-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![Room](https://img.shields.io/badge/Room-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![WorkManager](https://img.shields.io/badge/WorkManager-34A853?style=for-the-badge&logo=android&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

</div>

| الطبقة | المكتبات |
|---|---|
| الواجهة | Jetpack Compose · Material 3 · Splash Screen API |
| البيانات | Room · Security-Crypto |
| الخلفية | Kotlin Coroutines · WorkManager · VpnService · CallScreeningService |
| الأمان | AndroidX Biometric · PIN Lock |
| المسح | ZXing |

---

## 🚀 البناء والتشغيل

### ⚡ تلقائيًا عبر GitHub Actions
كل دفع إلى `main` يبني ملف **APK** تلقائيًا — حمّله من تبويب [Actions](https://github.com/NULL200OK/keep-safe/actions) ← آخر تشغيل ← **Artifacts**.

### 🛠️ محليًا

**المتطلبات:** Android Studio · JDK 17 · Android SDK 34

```bash
git clone https://github.com/NULL200OK/keep-safe.git
cd keep-safe
gradle wrapper --gradle-version 8.7
./gradlew assembleDebug
```

الملف الناتج: `app/build/outputs/apk/debug/app-debug.apk`

<details>
<summary><b>🔐 البناء الموقَّع (Release)</b></summary>

<br/>

أنشئ ملف `keystore.properties` في جذر المشروع (مستبعد من Git عمدًا):

```properties
storeFile=keepsafe-release.jks
storePassword=******
keyAlias=******
keyPassword=******
```

> ⚠️ لا ترفع ملفات `.jks` أو كلمات السر إلى المستودع أبدًا.

</details>

---

## 🗂️ هيكل المشروع

```text
app/src/main/java/com/nabilhassan/keepsafe/
├── screens/      شاشات الواجهة (الرئيسية، الإعدادات، VPN، Wi-Fi، QR …)
├── security/     القفل بالبصمة/PIN، فحص أمان الجهاز، فحص التسريبات
├── family/       فلتر DNS وإعدادات حماية العائلة
├── services/     VPN المحلي وفحص المكالمات
├── receivers/    استقبال SMS والتشغيل عند الإقلاع
├── workers/      التحديث الدوري والتقارير
├── export/       تصدير PDF و CSV
├── settings/     الإعدادات واللغات
└── ui/theme/     الألوان والخطوط والثيم
```

---

## 🗺️ خارطة الطريق

- [x] فحص الروابط والرسائل والمكالمات وAPK
- [x] VPN محلي وحماية الأطفال
- [x] قفل بالبصمة / PIN وفحص التسريبات
- [x] تقارير وتصدير PDF / CSV
- [ ] مزيد من اللغات
- [ ] تحسين قاعدة التهديدات

---

## 🤝 المساهمة

المساهمات مرحّب بها! افتح **Issue** أو أرسل **Pull Request**.

1. اعمل Fork للمشروع
2. أنشئ فرعًا: `git checkout -b feature/my-feature`
3. أرسل التعديلات: `git commit -m "Add my feature"`
4. افتح Pull Request

---

## 👥 الفريق والشكر

| الدور | الاسم |
|---|---|
| 🧑‍💻 **التصميم والتطوير** | **نبيل حسن** — [@NULL200OK](https://github.com/NULL200OK) |
| 🛠️ **المساعدة والمساهمة** | **dr2b** — [@Dr2b](https://github.com/Dr2b) |

---

## 📜 الرخصة

مرخّص بموجب **MIT** — راجع [LICENSE.md](LICENSE.md).

<div align="center">

**© 2026 Nabil Hassan (NULL200OK)**

إن أعجبك المشروع، لا تنسَ ⭐ النجمة!

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:2c5364,100:0f2027&height=110&section=footer" width="100%" alt=""/>

</div>
