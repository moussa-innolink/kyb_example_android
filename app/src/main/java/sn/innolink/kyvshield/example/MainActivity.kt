@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package sn.innolink.kyvshield.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import sn.innolink.kyvshield.lite.KyvshieldInput
import sn.innolink.kyvshield.lite.KyvshieldLite
import sn.innolink.kyvshield.lite.KyvshieldResultContract
import sn.innolink.kyvshield.lite.config.*
import sn.innolink.kyvshield.lite.result.*
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

// ─── Google Fonts provider ────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

private val PlusJakartaSans = GoogleFont("Plus Jakarta Sans")
private val JetBrainsMono   = GoogleFont("JetBrains Mono")

private val PlusJakartaSansFamily = FontFamily(
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = PlusJakartaSans, fontProvider = provider, weight = FontWeight.ExtraBold),
)

private val JetBrainsMonoFamily = FontFamily(
    Font(googleFont = JetBrainsMono, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = JetBrainsMono, fontProvider = provider, weight = FontWeight.Medium),
)

// ─── Localization strings ─────────────────────────────────────────────────────

private val strFr = mapOf(
    "appTitle" to "KyvShield Demo",
    "captureSteps" to "Étapes de capture",
    "selfie" to "Selfie",
    "selfieDesc" to "Photo du visage",
    "recto" to "Recto",
    "rectoDesc" to "Face avant du document",
    "verso" to "Verso",
    "versoDesc" to "Face arrière du document",
    "flowOptions" to "Options du flow",
    "introPage" to "Page d'introduction",
    "introPageDesc" to "Afficher la page d'accueil explicative",
    "instructionPages" to "Pages d'instruction",
    "instructionPagesDesc" to "Afficher les instructions avant chaque étape",
    "resultPage" to "Page de résultat",
    "resultPageDesc" to "Afficher le résumé final (sinon retour direct)",
    "successPerStep" to "Succès par étape",
    "successPerStepDesc" to "Animation de succès après chaque étape",
    "faceMatch" to "Face Match",
    "faceMatchDesc" to "Comparer le selfie avec la photo du document",
    "challengeAudio" to "Audio des challenges",
    "challengeAudioDesc" to "Jouer les instructions audio pour chaque challenge",
    "language" to "Langue",
    "securityLevel" to "Niveau de sécurité",
    "displayMode" to "Mode d'affichage",
    "selfieDisplay" to "Affichage selfie",
    "documentDisplay" to "Affichage document",
    "document" to "Document",
    "allCountries" to "Tous les pays",
    "autoDetect" to "Détection automatique",
    "apiOptions" to "Options API",
    "kycIdentifier" to "Identifiant KYC",
    "kycIdentifierHint" to "Optionnel: votre référence interne",
    "theme" to "Thème",
    "themeMode" to "Mode",
    "light" to "Clair",
    "dark" to "Sombre",
    "auto" to "Auto",
    "startVerification" to "Démarrer la vérification",
    "configuredFlow" to "Flux configuré:",
    "noStepConfigured" to "Aucune étape configurée",
    "starting" to "Démarrage...",
    "custom" to "Custom",
    "customColor" to "Couleur personnalisée",
    "cancel" to "Annuler",
    "apply" to "Appliquer",
    "themeInnolink" to "Innolink",
    "themeBlue" to "Bleu",
    "themeGreen" to "Vert",
    "themePurple" to "Violet",
    "themeKratos" to "Kratos",
    "themeLuna" to "Luna",
    "verificationCompleteSuccess" to "Vérification complète réussie",
    "verificationFailed" to "Vérification échouée",
    "documentsVerified" to "Documents vérifiés",
    "documentVerificationFailed" to "Vérification documents échouée",
    "rectoFaceVerified" to "Recto + Face vérifiés",
    "rectoVerified" to "Recto vérifié",
    "rectoVerificationFailed" to "Vérification recto échouée",
    "versoVerified" to "Verso vérifié",
    "versoVerificationFailed" to "Vérification verso échouée",
    "selfieCaptured" to "Selfie capturé",
    "selfieFailed" to "Selfie échoué",
    "completed" to "Terminé",
    "cancelled" to "Annulé",
    "authenticityScore" to "Score d'authenticité",
    "scannedDocuments" to "Documents scannés",
    "extractedPhotos" to "Photos extraites du document",
    "extractedPhoto" to "Photo extraite",
    "humanFace" to "Visage Humain",
    "faceMatchResult" to "Visage correspondant",
    "faceNoMatchResult" to "Visage non correspondant",
    "componentScores" to "Scores des composants",
    "overallScore" to "Score Global",
    "liveness" to "Vivacité",
    "processingTime" to "Temps de traitement",
    "total" to "Total",
    "imageSavedToGallery" to "Image enregistrée dans la galerie",
    "cameraRequired" to "Caméra requise",
    "cameraAccessDenied" to "L'accès à la caméra a été refusé. Veuillez l'activer dans les réglages de votre appareil pour continuer la vérification.",
    "openSettings" to "Ouvrir les réglages",
    "selfieDisplayStandard" to "Standard",
    "selfieDisplayCompact" to "Compact",
    "selfieDisplayImmersive" to "Immersif",
    "challengeBottom" to "Challenge en bas",
    "challengeOnCamera" to "Challenge sur caméra",
    "fullScreen" to "Plein écran",
    "docDisplayStandard" to "Standard",
    "docDisplayCompact" to "Compact",
    "docDisplayImmersive" to "Immersif",
    "instructionsBottom" to "Instructions en bas",
    "instructionsOnCamera" to "Instructions sur caméra",
    "mrz" to "MRZ (Machine Readable Zone)",
    "fields" to "champs",
    "noExtractedData" to "Aucune donnée extraite",
    "none" to "Aucun",
    "result" to "Résultat",
    "notDetected" to "Non détecté",
    "threshold" to "Seuil",
    "selfieCapturedNoAnalysis" to "Selfie capturé avec succès. Aucune analyse de document effectuée.",
    "errorDecodeImage" to "Erreur: impossible de décoder l'image",
    "intro" to "Intro",
    "instructions" to "Instructions",
    "amlScreening" to "Screening AML / Sanctions",
    "amlStatus" to "Statut",
    "amlRiskLevel" to "Niveau de risque",
    "amlMatches" to "Correspondances",
    "amlClear" to "Aucune correspondance",
    "amlMatch" to "Correspondance trouvée",
    "amlError" to "Erreur de screening",
    "amlDisabled" to "Désactivé",
    "selectAtLeastOneStep" to "Sélectionnez au moins une étape",
    "noDocumentAvailable" to "Aucun document disponible",
    "selectDocument" to "Sélectionner un document",
    "rectoVerso" to "Recto + Verso",
    "rectoOnly" to "Recto uniquement",
    "rectoOnlySupported" to "ne supporte que le recto",
    "captureOptions" to "Options de capture",
    "display" to "Affichage",
    "selectAtLeastOneStepAbove" to "Sélectionnez au moins une étape ci-dessus",
    "flowOptionsSection" to "Options de flux",
    "loading" to "Chargement...",
    "error" to "Erreur",
    "documentType" to "Type de document",
)

private val strEn = mapOf(
    "appTitle" to "KyvShield Demo",
    "captureSteps" to "Capture Steps",
    "selfie" to "Selfie",
    "selfieDesc" to "Face photo",
    "recto" to "Recto",
    "rectoDesc" to "Document front side",
    "verso" to "Verso",
    "versoDesc" to "Document back side",
    "flowOptions" to "Flow Options",
    "introPage" to "Introduction Page",
    "introPageDesc" to "Show explanatory welcome page",
    "instructionPages" to "Instruction Pages",
    "instructionPagesDesc" to "Show instructions before each step",
    "resultPage" to "Result Page",
    "resultPageDesc" to "Show final summary (otherwise direct return)",
    "successPerStep" to "Success Per Step",
    "successPerStepDesc" to "Success animation after each step",
    "faceMatch" to "Face Match",
    "faceMatchDesc" to "Compare selfie with document photo",
    "challengeAudio" to "Challenge Audio",
    "challengeAudioDesc" to "Play audio instructions for each challenge",
    "language" to "Language",
    "securityLevel" to "Security Level",
    "displayMode" to "Display Mode",
    "selfieDisplay" to "Selfie Display",
    "documentDisplay" to "Document Display",
    "document" to "Document",
    "allCountries" to "All Countries",
    "autoDetect" to "Auto Detect",
    "apiOptions" to "API Options",
    "kycIdentifier" to "KYC Identifier",
    "kycIdentifierHint" to "Optional: your internal reference",
    "theme" to "Theme",
    "themeMode" to "Mode",
    "light" to "Light",
    "dark" to "Dark",
    "auto" to "Auto",
    "startVerification" to "Start Verification",
    "configuredFlow" to "Configured flow:",
    "noStepConfigured" to "No step configured",
    "starting" to "Starting...",
    "custom" to "Custom",
    "customColor" to "Custom Color",
    "cancel" to "Cancel",
    "apply" to "Apply",
    "themeInnolink" to "Innolink",
    "themeBlue" to "Blue",
    "themeGreen" to "Green",
    "themePurple" to "Purple",
    "themeKratos" to "Kratos",
    "themeLuna" to "Luna",
    "verificationCompleteSuccess" to "Full verification successful",
    "verificationFailed" to "Verification failed",
    "documentsVerified" to "Documents verified",
    "documentVerificationFailed" to "Document verification failed",
    "rectoFaceVerified" to "Front + Face verified",
    "rectoVerified" to "Front verified",
    "rectoVerificationFailed" to "Front verification failed",
    "versoVerified" to "Back verified",
    "versoVerificationFailed" to "Back verification failed",
    "selfieCaptured" to "Selfie captured",
    "selfieFailed" to "Selfie failed",
    "completed" to "Completed",
    "cancelled" to "Cancelled",
    "authenticityScore" to "Authenticity Score",
    "scannedDocuments" to "Scanned Documents",
    "extractedPhotos" to "Photos extracted from document",
    "extractedPhoto" to "Extracted photo",
    "humanFace" to "Human Face",
    "faceMatchResult" to "Face match",
    "faceNoMatchResult" to "Face mismatch",
    "componentScores" to "Component Scores",
    "overallScore" to "Overall Score",
    "liveness" to "Liveness",
    "processingTime" to "Processing Time",
    "total" to "Total",
    "imageSavedToGallery" to "Image saved to gallery",
    "cameraRequired" to "Camera Required",
    "cameraAccessDenied" to "Camera access was denied. Please enable it in your device settings to continue verification.",
    "openSettings" to "Open Settings",
    "selfieDisplayStandard" to "Standard",
    "selfieDisplayCompact" to "Compact",
    "selfieDisplayImmersive" to "Immersive",
    "challengeBottom" to "Challenge at bottom",
    "challengeOnCamera" to "Challenge on camera",
    "fullScreen" to "Full screen",
    "docDisplayStandard" to "Standard",
    "docDisplayCompact" to "Compact",
    "docDisplayImmersive" to "Immersive",
    "instructionsBottom" to "Instructions at bottom",
    "instructionsOnCamera" to "Instructions on camera",
    "mrz" to "MRZ (Machine Readable Zone)",
    "fields" to "fields",
    "noExtractedData" to "No extracted data",
    "none" to "None",
    "result" to "Result",
    "notDetected" to "Not detected",
    "threshold" to "Threshold",
    "selfieCapturedNoAnalysis" to "Selfie captured successfully. No document analysis performed.",
    "errorDecodeImage" to "Error: unable to decode image",
    "intro" to "Intro",
    "instructions" to "Instructions",
    "amlScreening" to "AML / Sanctions Screening",
    "amlStatus" to "Status",
    "amlRiskLevel" to "Risk Level",
    "amlMatches" to "Matches",
    "amlClear" to "No matches found",
    "amlMatch" to "Match found",
    "amlError" to "Screening error",
    "amlDisabled" to "Disabled",
    "selectAtLeastOneStep" to "Select at least one step",
    "noDocumentAvailable" to "No document available",
    "selectDocument" to "Select a document",
    "rectoVerso" to "Front + Back",
    "rectoOnly" to "Front only",
    "rectoOnlySupported" to "only supports front side",
    "captureOptions" to "Capture Options",
    "display" to "Display",
    "selectAtLeastOneStepAbove" to "Select at least one step above",
    "flowOptionsSection" to "Flow Options",
    "loading" to "Loading...",
    "error" to "Error",
    "documentType" to "Document Type",
)

private val strWo = mapOf(
    "appTitle" to "KyvShield Demo",
    "captureSteps" to "Etapes yi",
    "selfie" to "Selfie",
    "selfieDesc" to "Nataal kanam",
    "recto" to "Kanam",
    "rectoDesc" to "Kanam kaart bi",
    "verso" to "Ginnaaw",
    "versoDesc" to "Ginnaaw kaart bi",
    "flowOptions" to "Tànneef yi",
    "introPage" to "Xëtu njëkk",
    "introPageDesc" to "Wone xëtu njëkk bi",
    "instructionPages" to "Xëtu ndimbal",
    "instructionPagesDesc" to "Wone ndigal yi balaa etap bu nekk",
    "resultPage" to "Xëtu gisaat",
    "resultPageDesc" to "Wone jeexital bi",
    "successPerStep" to "Yëgge etap",
    "successPerStepDesc" to "Wone yëgge bu baax",
    "faceMatch" to "Seetu kanam",
    "faceMatchDesc" to "Seetante selfie ak nataal kaart bi",
    "challengeAudio" to "Audio jëf yi",
    "challengeAudioDesc" to "Déglu ndigal audio yi",
    "language" to "Làkk",
    "securityLevel" to "Tolluwaayu kaarange",
    "displayMode" to "Anamu wone",
    "selfieDisplay" to "Anamu selfie",
    "documentDisplay" to "Anamu kaart",
    "document" to "Kaart",
    "allCountries" to "Réew yépp",
    "autoDetect" to "Seet ci boppam",
    "apiOptions" to "Tànneef API",
    "kycIdentifier" to "Tànneefug KYC",
    "kycIdentifierHint" to "Wakhtaanul: sa référence",
    "theme" to "Anamu",
    "themeMode" to "Anamu",
    "light" to "Leer",
    "dark" to "Lëndëm",
    "auto" to "Auto",
    "startVerification" to "Tambali seet bi",
    "configuredFlow" to "Flux bi ci:",
    "noStepConfigured" to "Amul etap bu am",
    "starting" to "Tambali...",
    "custom" to "Custom",
    "customColor" to "Melo bu ëpp",
    "cancel" to "Neenal",
    "apply" to "Jëfandikoo",
    "themeInnolink" to "Innolink",
    "themeBlue" to "Baxa",
    "themeGreen" to "Wert",
    "themePurple" to "Violet",
    "themeKratos" to "Kratos",
    "themeLuna" to "Luna",
    "verificationCompleteSuccess" to "Seet bi baax na yépp",
    "verificationFailed" to "Seet bi baxtul",
    "documentsVerified" to "Kaart yi seetees nañu",
    "documentVerificationFailed" to "Seet kaart yi baxtul",
    "rectoFaceVerified" to "Kanam + Kanam seetees nañu",
    "rectoVerified" to "Kanam bi seetees na",
    "rectoVerificationFailed" to "Seet kanam bi baxtul",
    "versoVerified" to "Ginnaaw bi seetees na",
    "versoVerificationFailed" to "Seet ginnaaw bi baxtul",
    "selfieCaptured" to "Selfie nataaloo na",
    "selfieFailed" to "Selfie baxtul",
    "completed" to "Jeex na",
    "cancelled" to "Dindi nañu",
    "authenticityScore" to "Notu dëgg",
    "scannedDocuments" to "Kaart yi seetees nañu",
    "extractedPhotos" to "Nataal yi ci kaart bi",
    "extractedPhoto" to "Nataal bu àgg",
    "humanFace" to "Kanam nit",
    "faceMatchResult" to "Kanam yi mën nañu",
    "faceNoMatchResult" to "Kanam yi mënuñu",
    "componentScores" to "Not yi",
    "overallScore" to "Not bu mag",
    "liveness" to "Dund",
    "processingTime" to "Waxtu jëf bi",
    "total" to "Yépp",
    "imageSavedToGallery" to "Nataal bi denc nañu ko ci galerie",
    "cameraRequired" to "Kaméra waral na",
    "cameraAccessDenied" to "Kaméra bi tëj nañu la. Ubbil ko ci tànneef appareil bi.",
    "openSettings" to "Ubbi tànneef yi",
    "selfieDisplayStandard" to "Standard",
    "selfieDisplayCompact" to "Compact",
    "selfieDisplayImmersive" to "Immersif",
    "challengeBottom" to "Challenge ci suuf",
    "challengeOnCamera" to "Challenge ci kaméra",
    "fullScreen" to "Ecran bu mat",
    "docDisplayStandard" to "Standard",
    "docDisplayCompact" to "Compact",
    "docDisplayImmersive" to "Immersif",
    "instructionsBottom" to "Ndigal ci suuf",
    "instructionsOnCamera" to "Ndigal ci kaméra",
    "mrz" to "MRZ (Machine Readable Zone)",
    "fields" to "tomb",
    "noExtractedData" to "Amul njoxe",
    "none" to "Amul",
    "result" to "Njeexital",
    "notDetected" to "Gisul ko",
    "threshold" to "Palier",
    "selfieCapturedNoAnalysis" to "Selfie nataaloo na. Amul seet kaart bu amees.",
    "errorDecodeImage" to "Njumte: mënu ko decode nataal bi",
    "intro" to "Intro",
    "instructions" to "Ndigal",
    "amlScreening" to "Seet AML / Sanctions",
    "amlStatus" to "Wàllu",
    "amlRiskLevel" to "Tolluwaayu riskk",
    "amlMatches" to "Seetante",
    "amlClear" to "Amul seetante",
    "amlMatch" to "Am na seetante",
    "amlError" to "Njumte ci seet bi",
    "amlDisabled" to "Tëj nañu ko",
    "selectAtLeastOneStep" to "Tànnal benn etap bu mag",
    "noDocumentAvailable" to "Amul kaart bu am",
    "selectDocument" to "Tànnal benn kaart",
    "rectoVerso" to "Kanam + Ginnaaw",
    "rectoOnly" to "Kanam rekk",
    "rectoOnlySupported" to "kanam rekk la mën",
    "captureOptions" to "Tànneef nataal",
    "display" to "Woneg",
    "selectAtLeastOneStepAbove" to "Tànnal benn etap ci kaw",
    "flowOptionsSection" to "Tànneef yi",
    "loading" to "Di yegge...",
    "error" to "Njumte",
    "documentType" to "Xeeti kaart",
)

private val allStrings = mapOf("fr" to strFr, "en" to strEn, "wo" to strWo)

// ─── Colors ───────────────────────────────────────────────────────────────────

private val ColorDefaultPrimary = Color(0xFFEF8352)
private val ColorBlue           = Color(0xFF3B82F6)
private val ColorGreen          = Color(0xFF10B981)
private val ColorPurple         = Color(0xFF8B5CF6)
private val ColorKratos         = Color(0xFF00377D)
private val ColorLuna           = Color(0xFFFFD100)

private val themeColorMap = mapOf(
    "default" to ColorDefaultPrimary,
    "blue"    to ColorBlue,
    "green"   to ColorGreen,
    "purple"  to ColorPurple,
    "kratos"  to ColorKratos,
    "luna"    to ColorLuna,
)

// ─── MainActivity ─────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { KyvShieldDemoApp() }
    }
}

// ─── Root composable ──────────────────────────────────────────────────────────

@Composable
fun KyvShieldDemoApp() {
    KyvShieldDemoPage()
}

// ─── Demo page state ──────────────────────────────────────────────────────────

@Composable
fun KyvShieldDemoPage() {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // ── Language
    var selectedLanguage by remember { mutableStateOf("fr") }
    fun t(key: String): String = allStrings[selectedLanguage]?.get(key) ?: allStrings["fr"]?.get(key) ?: key

    // ── Theme / brightness
    var selectedTheme   by remember { mutableStateOf("default") }
    var customColor     by remember { mutableStateOf(Color(0xFFEF8352)) }
    var brightnessMode  by remember { mutableStateOf<String?>("light") } // null = auto

    val primaryColor: Color = when {
        selectedTheme == "custom" -> customColor
        else -> themeColorMap[selectedTheme] ?: ColorDefaultPrimary
    }

    val isDark: Boolean = when (brightnessMode) {
        "dark"  -> true
        "light" -> false
        else    -> isSystemInDarkTheme()
    }

    // Design-system colors
    val bgColor       = if (isDark) Color(0xFF0F172A) else Color(0xFFF9FAFB)
    val cardColor     = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val textPrimary   = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111827)
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF4B5563)
    val textTertiary  = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280)
    val borderColor   = if (isDark) Color(0xFF334155) else Color(0xFFE5E7EB)
    val inputBg       = if (isDark) Color(0xFF1E293B) else Color(0xFFF9FAFB)
    val inputBorder   = if (isDark) Color(0xFF334155) else Color(0xFFD1D5DB)
    val chipBg        = if (isDark) Color(0xFF334155) else Color(0xFFF3F4F6)
    val chipText      = if (isDark) Color(0xFFCBD5E1) else Color(0xFF374151)

    // ── Capture steps
    var captureSelfie by remember { mutableStateOf(false) }
    var captureRecto  by remember { mutableStateOf(false) }
    var captureVerso  by remember { mutableStateOf(false) }

    // ── Challenge modes
    var selfieChallengeMode by remember { mutableStateOf(ChallengeMode.minimal) }
    var rectoChallengeMode  by remember { mutableStateOf(ChallengeMode.minimal) }
    var versoChallengeMode  by remember { mutableStateOf(ChallengeMode.minimal) }

    // ── Display modes
    var selfieDisplayMode   by remember { mutableStateOf(SelfieDisplayMode.standard) }
    var documentDisplayMode by remember { mutableStateOf(DocumentDisplayMode.standard) }

    // ── Flow options
    var showIntroPage        by remember { mutableStateOf(false) }
    var showInstructionPages by remember { mutableStateOf(true) }
    var showResultPage       by remember { mutableStateOf(false) }
    var showSuccessPerStep   by remember { mutableStateOf(true) }
    var requireFaceMatch     by remember { mutableStateOf(false) }
    var playChallengeAudio   by remember { mutableStateOf(true) }

    // ── KYC identifier
    var kycIdentifier by remember { mutableStateOf("") }

    // ── Documents
    var documentTypes       by remember { mutableStateOf<List<KyvshieldDocument>>(emptyList()) }
    var selectedDocument    by remember { mutableStateOf<KyvshieldDocument?>(null) }
    var selectedCountry     by remember { mutableStateOf<String?>(null) }
    var isLoadingDocuments  by remember { mutableStateOf(true) }

    // ── Result / loading
    var lastResult by remember { mutableStateOf<KYCResult?>(null) }
    var isLoading  by remember { mutableStateOf(false) }

    // ── Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Image popup
    var imagePopupData  by remember { mutableStateOf<Pair<ByteArray, String>?>(null) }

    // ── Color picker dialog
    var showColorPicker by remember { mutableStateOf(false) }

    // ── Document dropdown expanded
    var documentDropdownExpanded by remember { mutableStateOf(false) }

    val apiBaseUrl = "https://kyvshield-naruto.innolinkcloud.com"
    val apiKey     = "kyvshield_demo_key_2024"

    val filteredDocuments: List<KyvshieldDocument> = remember(documentTypes, selectedCountry) {
        if (selectedCountry == null) documentTypes
        else documentTypes.filter { it.country == selectedCountry }
    }

    val availableCountries: List<Pair<String, String>> = remember(documentTypes) {
        val seen = mutableSetOf<String>()
        documentTypes.mapNotNull { doc ->
            if (seen.add(doc.country)) {
                val label = if (doc.countryName.isNotEmpty()) doc.countryName else doc.country
                Pair(doc.country, label)
            } else null
        }
    }

    // ── Fetch documents
    suspend fun fetchDocuments() {
        isLoadingDocuments = true
        try {
            val result = withContext(Dispatchers.IO) {
                val conn = URL("$apiBaseUrl/api/v1/documents").openConnection() as HttpURLConnection
                conn.setRequestProperty("X-API-Key", apiKey)
                conn.connectTimeout = 10_000
                conn.readTimeout    = 10_000
                val body = conn.inputStream.bufferedReader().readText()
                conn.disconnect()
                body
            }
            val json = JSONObject(result)
            val arr  = json.optJSONArray("documents") ?: return
            val docs = (0 until arr.length())
                .map { KyvshieldDocument.fromJson(arr.getJSONObject(it)) }
                .filter { it.enabled }
            documentTypes = docs
            if (selectedDocument != null) {
                val match = docs.firstOrNull { it.docType == selectedDocument!!.docType }
                selectedDocument = match ?: docs.firstOrNull()
            } else {
                selectedDocument = docs.firstOrNull()
            }
        } catch (_: Exception) {
        } finally {
            isLoadingDocuments = false
        }
    }

    LaunchedEffect(Unit) { fetchDocuments() }

    // Camera permission result → triggers KYC or shows settings dialog
    var cameraPermGranted by remember { mutableStateOf(false) }
    val cameraPermLauncher = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermGranted = granted
    }

    // ── KYC launcher
    val kycLauncher = rememberLauncherForActivityResult(KyvshieldResultContract()) { result ->
        lastResult = result
        isLoading  = false
    }

    fun buildFlowSummary(): String {
        val steps = buildList {
            if (captureSelfie) add(CaptureStep.selfie)
            if (captureRecto)  add(CaptureStep.recto)
            if (captureVerso)  add(CaptureStep.verso)
        }
        val parts = mutableListOf<String>()
        val docCode = selectedDocument?.docType ?: t("none")
        parts.add("[$docCode]")
        if (showIntroPage) parts.add(t("intro"))
        for (step in steps) {
            if (showInstructionPages) parts.add("${t("instructions")} ${step.name.replaceFirstChar { it.uppercase() }}")
            val mode = when (step) {
                CaptureStep.selfie -> selfieChallengeMode
                CaptureStep.recto  -> rectoChallengeMode
                CaptureStep.verso  -> versoChallengeMode
            }
            val modeShort = when (mode) {
                ChallengeMode.minimal  -> "min"
                ChallengeMode.standard -> "std"
                ChallengeMode.strict   -> "strict"
            }
            parts.add("${step.name.replaceFirstChar { it.uppercase() }}[$modeShort]")
        }
        if (showResultPage) parts.add(t("result"))
        return if (parts.size <= 1) "[$docCode] - ${t("noStepConfigured")}"
        else parts.joinToString(" → ")
    }

    var showCameraPermDialog by mutableStateOf(false)
    var pendingKycAfterPermission by mutableStateOf(false)

    // When camera permission granted after request, auto-start KYC
    LaunchedEffect(cameraPermGranted) {
        if (cameraPermGranted && pendingKycAfterPermission) {
            pendingKycAfterPermission = false
            // Re-trigger startKyc — permission is now granted, it will pass the check
        }
    }

    fun startKyc() {
        val steps = buildList {
            if (captureSelfie) add(CaptureStep.selfie)
            if (captureRecto)  add(CaptureStep.recto)
            if (captureVerso)  add(CaptureStep.verso)
        }
        if (steps.isEmpty()) {
            scope.launch { snackbarHostState.showSnackbar(t("selectAtLeastOneStep")) }
            return
        }
        // Camera permission: check → request popup → settings dialog (same flow as Flutter)
        val camPerm = android.Manifest.permission.CAMERA
        val camStatus = androidx.core.content.ContextCompat.checkSelfPermission(context, camPerm)
        if (camStatus != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val activity = context as? android.app.Activity
            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(camPerm) ?: false
            if (!shouldShowRationale && !cameraPermGranted) {
                // First time or "don't ask again" not yet set → show system permission popup
                pendingKycAfterPermission = true
                cameraPermLauncher.launch(camPerm)
            } else {
                // Permanently denied → show "Open Settings" dialog
                showCameraPermDialog = true
            }
            return
        }

        isLoading = true
        val stepModes = buildMap<CaptureStep, ChallengeMode> {
            if (captureSelfie) put(CaptureStep.selfie, selfieChallengeMode)
            if (captureRecto)  put(CaptureStep.recto,  rectoChallengeMode)
            if (captureVerso)  put(CaptureStep.verso,  versoChallengeMode)
        }
        val themeColor = if (selectedTheme == "custom") customColor else themeColorMap[selectedTheme] ?: ColorDefaultPrimary
        val config = KyvshieldConfig(
            baseUrl   = apiBaseUrl,
            apiKey    = apiKey,
            enableLog = true,
            theme     = KyvshieldThemeConfig(
                primaryColor = themeColor.toArgb(),
                darkMode     = when (brightnessMode) { "dark" -> true; "light" -> false; else -> null }
            )
        )
        val flow = KyvshieldFlowConfig(
            steps               = steps,
            challengeMode       = ChallengeMode.minimal,
            stepChallengeModes  = stepModes,
            selfieDisplayMode   = selfieDisplayMode,
            documentDisplayMode = documentDisplayMode,
            showIntroPage       = showIntroPage,
            showInstructionPages = showInstructionPages,
            showResultPage      = showResultPage,
            showSuccessPerStep  = showSuccessPerStep,
            requireFaceMatch    = requireFaceMatch,
            language            = selectedLanguage,
            target              = selectedDocument,
            kycIdentifier       = kycIdentifier.trim().ifEmpty { null },
            playChallengeAudio  = playChallengeAudio,
        )
        kycLauncher.launch(KyvshieldInput(config, flow))
    }

    fun healthCheck() {
        scope.launch {
            try {
                val body = withContext(Dispatchers.IO) {
                    val conn = URL("$apiBaseUrl/health").openConnection() as HttpURLConnection
                    conn.connectTimeout = 10_000; conn.readTimeout = 10_000
                    val b = conn.inputStream.bufferedReader().readText()
                    conn.disconnect(); b
                }
                val data = JSONObject(body)
                snackbarHostState.showSnackbar("Health: ${data.optString("status", "OK")} (200)")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Health check failed: $e")
            }
        }
    }

    fun validateKey() {
        scope.launch {
            try {
                val (code, body) = withContext(Dispatchers.IO) {
                    val conn = URL("$apiBaseUrl/api/v1/validate-key").openConnection() as HttpURLConnection
                    conn.setRequestProperty("X-API-Key", apiKey)
                    conn.connectTimeout = 10_000; conn.readTimeout = 10_000
                    val b = conn.inputStream.bufferedReader().readText()
                    Pair(conn.responseCode, b).also { conn.disconnect() }
                }
                val data = JSONObject(body)
                if (code == 200) snackbarHostState.showSnackbar("API Key valid: ${data.optString("application", data.optString("app_name", "OK"))}")
                else snackbarHostState.showSnackbar("Invalid key: ${data.optString("error", code.toString())}")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Validation failed: $e")
            }
        }
    }

    fun saveImageToGallery(bytes: ByteArray, title: String) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext
                    val stream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                    val fileName = "${title.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
                    val values = android.content.ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/KyvShield")
                    }
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    uri?.let { context.contentResolver.openOutputStream(it)?.use { os -> os.write(stream.toByteArray()) } }
                }
                snackbarHostState.showSnackbar(t("imageSavedToGallery"))
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("${t("error")}: $e")
            }
        }
    }

    // ─── Color scheme wiring ──────────────────────────────────────────────────

    val colorScheme = if (isDark) darkColorScheme(primary = primaryColor) else lightColorScheme(primary = primaryColor)

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = bgColor,
        ) { paddingValues ->
            // Image popup overlay
            imagePopupData?.let { (bytes, title) ->
                ImagePopupDialog(
                    imageBytes = bytes,
                    title      = title,
                    onDismiss  = { imagePopupData = null },
                    onSave     = { saveImageToGallery(bytes, title) },
                    chipText   = chipText,
                    textPrimary = textPrimary,
                )
            }

            // Camera permission dialog
            if (showCameraPermDialog) {
                AlertDialog(
                    onDismissRequest = { showCameraPermDialog = false },
                    icon = { Text("\uD83D\uDCF7", fontSize = 28.sp) }, // Camera emoji
                    title = { Text(t("cameraRequired"), fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold) },
                    text = { Text(t("cameraAccessDenied"), fontFamily = PlusJakartaSansFamily, fontSize = 14.sp, color = chipText) },
                    dismissButton = {
                        TextButton(onClick = { showCameraPermDialog = false }) {
                            Text(t("cancel"), fontFamily = PlusJakartaSansFamily, color = textTertiary)
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showCameraPermDialog = false
                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text(t("openSettings"), fontFamily = PlusJakartaSansFamily, color = Color.White)
                        }
                    },
                )
            }

            // Color picker dialog
            if (showColorPicker) {
                ColorPickerDialog(
                    currentColor = customColor,
                    titleText    = t("customColor"),
                    cancelText   = t("cancel"),
                    applyText    = t("apply"),
                    onDismiss    = { showColorPicker = false },
                    onApply      = { color ->
                        customColor   = color
                        selectedTheme = "custom"
                        showColorPicker = false
                    },
                    fonts = PlusJakartaSansFamily,
                    cardColor = cardColor,
                    textPrimary = textPrimary,
                )
            }

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Entry animations (staggered fade + slide)
                    var didAppear by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { kotlinx.coroutines.delay(50); didAppear = true }

                    val headerAlpha by animateFloatAsState(if (didAppear) 1f else 0f, tween(400, delayMillis = 100), label = "ha")
                    val headerOffset by animateDpAsState(if (didAppear) 0.dp else 16.dp, tween(400, delayMillis = 100), label = "ho")
                    val configAlpha by animateFloatAsState(if (didAppear) 1f else 0f, tween(400, delayMillis = 200), label = "ca")
                    val configOffset by animateDpAsState(if (didAppear) 0.dp else 16.dp, tween(400, delayMillis = 200), label = "co")
                    val buttonsAlpha by animateFloatAsState(if (didAppear) 1f else 0f, tween(400, delayMillis = 300), label = "ba")
                    val buttonsOffset by animateDpAsState(if (didAppear) 0.dp else 16.dp, tween(400, delayMillis = 300), label = "bo")
                    val resultAlpha by animateFloatAsState(if (didAppear) 1f else 0f, tween(400, delayMillis = 400), label = "ra")
                    val resultOffset by animateDpAsState(if (didAppear) 0.dp else 16.dp, tween(400, delayMillis = 400), label = "ro")

                    Spacer(Modifier.windowInsetsTopHeight(androidx.compose.foundation.layout.WindowInsets.statusBars))
                    Spacer(Modifier.height(8.dp))

                    // ── Header
                    Box(Modifier.graphicsLayer { alpha = headerAlpha; translationY = headerOffset.toPx() }) {
                        HeaderSection(primaryColor = primaryColor, textPrimary = textPrimary, textSecondary = textSecondary, fonts = PlusJakartaSansFamily)
                    }
                    Spacer(Modifier.height(32.dp))

                    // ── Config card
                    Box(Modifier.graphicsLayer { alpha = configAlpha; translationY = configOffset.toPx() }) {
                    ConfigCard(
                        primaryColor         = primaryColor,
                        cardColor            = cardColor,
                        borderColor          = borderColor,
                        textPrimary          = textPrimary,
                        textSecondary        = textSecondary,
                        textTertiary         = textTertiary,
                        inputBg              = inputBg,
                        inputBorder          = inputBorder,
                        chipBg               = chipBg,
                        chipText             = chipText,
                        fonts                = PlusJakartaSansFamily,
                        monoFonts            = JetBrainsMonoFamily,
                        t                    = ::t,
                        isLoadingDocuments   = isLoadingDocuments,
                        filteredDocuments    = filteredDocuments,
                        availableCountries   = availableCountries,
                        selectedDocument     = selectedDocument,
                        selectedCountry      = selectedCountry,
                        documentDropdownExpanded = documentDropdownExpanded,
                        onDocumentDropdownToggle = { documentDropdownExpanded = !documentDropdownExpanded },
                        onDocumentDropdownDismiss = { documentDropdownExpanded = false },
                        onCountrySelected    = { code ->
                            selectedCountry = code
                            if (code != null && selectedDocument?.country != code) {
                                val filtered = documentTypes.filter { it.country == code }
                                selectedDocument = filtered.firstOrNull()
                            }
                        },
                        onDocumentSelected   = { doc ->
                            selectedDocument = doc
                            if (doc != null && !doc.hasVerso) versoChallengeMode = ChallengeMode.minimal
                        },
                        captureSelfie        = captureSelfie,
                        captureRecto         = captureRecto,
                        captureVerso         = captureVerso,
                        onSelfieToggle       = { captureSelfie = it },
                        onRectoToggle        = { captureRecto = it },
                        onVersoToggle        = { captureVerso = it },
                        selfieDisplayMode    = selfieDisplayMode,
                        documentDisplayMode  = documentDisplayMode,
                        onSelfieDisplayMode  = { selfieDisplayMode = it },
                        onDocumentDisplayMode = { documentDisplayMode = it },
                        selfieChallengeMode  = selfieChallengeMode,
                        rectoChallengeMode   = rectoChallengeMode,
                        versoChallengeMode   = versoChallengeMode,
                        onSelfieChallengeMode = { selfieChallengeMode = it },
                        onRectoChallengeMode  = { rectoChallengeMode = it },
                        onVersoChallengeMode  = { versoChallengeMode = it },
                        showIntroPage        = showIntroPage,
                        showInstructionPages = showInstructionPages,
                        showResultPage       = showResultPage,
                        showSuccessPerStep   = showSuccessPerStep,
                        requireFaceMatch     = requireFaceMatch,
                        playChallengeAudio   = playChallengeAudio,
                        onShowIntroPage      = { showIntroPage = it },
                        onShowInstructionPages = { showInstructionPages = it },
                        onShowResultPage     = { showResultPage = it },
                        onShowSuccessPerStep = { showSuccessPerStep = it },
                        onRequireFaceMatch   = { requireFaceMatch = it },
                        onPlayChallengeAudio = { playChallengeAudio = it },
                        kycIdentifier        = kycIdentifier,
                        onKycIdentifierChange = { kycIdentifier = it },
                        selectedLanguage     = selectedLanguage,
                        onLanguageSelected   = { selectedLanguage = it },
                        selectedTheme        = selectedTheme,
                        customColor          = customColor,
                        onThemeSelected      = { selectedTheme = it },
                        onShowColorPicker    = { showColorPicker = true },
                        brightnessMode       = brightnessMode,
                        onBrightnessModeChange = { brightnessMode = it },
                        flowSummary          = buildFlowSummary(),
                    )
                    } // close Config card animation Box
                    Spacer(Modifier.height(24.dp))

                    // ── Action buttons
                    Box(Modifier.graphicsLayer { alpha = buttonsAlpha; translationY = buttonsOffset.toPx() }) {
                    ActionButtons(
                        primaryColor  = primaryColor,
                        borderColor   = borderColor,
                        textSecondary = textSecondary,
                        fonts         = PlusJakartaSansFamily,
                        isLoading     = isLoading,
                        startLabel    = t("startVerification"),
                        startingLabel = t("starting"),
                        onStart       = { startKyc() },
                        onHealthCheck = { healthCheck() },
                        onValidateKey = { validateKey() },
                    )
                    } // close Action buttons animation Box
                    Spacer(Modifier.height(24.dp))

                    // ── Result section
                    Box(Modifier.graphicsLayer { alpha = resultAlpha; translationY = resultOffset.toPx() }) {
                    lastResult?.let { result ->
                        ResultSection(
                            result           = result,
                            primaryColor     = primaryColor,
                            cardColor        = cardColor,
                            borderColor      = borderColor,
                            textPrimary      = textPrimary,
                            textSecondary    = textSecondary,
                            textTertiary     = textTertiary,
                            inputBg          = inputBg,
                            inputBorder      = inputBorder,
                            chipBg           = chipBg,
                            chipText         = chipText,
                            fonts            = PlusJakartaSansFamily,
                            monoFonts        = JetBrainsMonoFamily,
                            requireFaceMatch = requireFaceMatch,
                            t                = ::t,
                            onImageClick     = { bytes, title -> imagePopupData = Pair(bytes, title) },
                            onCopySessionId  = { id ->
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("Session ID", id))
                                scope.launch { snackbarHostState.showSnackbar("Session ID: $id") }
                            },
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                    } // close Result section animation Box
                }

                // Pull-to-refresh: swipe down on top triggers document reload
                // Note: Full pull-to-refresh requires Compose Material 1.4+ pullRefresh modifier.
                // For now, documents refresh on app launch via LaunchedEffect(Unit).
            }
        }
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────

@Composable
fun HeaderSection(primaryColor: Color, textPrimary: Color, textSecondary: Color, fonts: FontFamily) {
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "logoScale")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(RoundedCornerShape(20.dp))
                .background(primaryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            ShieldIcon(tint = primaryColor, size = 40.dp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text       = "KyvShield SDK",
            style      = TextStyle(fontFamily = fonts, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textPrimary),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "CIN Verification Demo",
            style = TextStyle(fontFamily = fonts, fontSize = 16.sp, color = textSecondary),
        )
    }
}

// ─── Config Card ──────────────────────────────────────────────────────────────

@Composable
fun ConfigCard(
    primaryColor: Color,
    cardColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    inputBg: Color,
    inputBorder: Color,
    chipBg: Color,
    chipText: Color,
    fonts: FontFamily,
    monoFonts: FontFamily,
    t: (String) -> String,
    isLoadingDocuments: Boolean,
    filteredDocuments: List<KyvshieldDocument>,
    availableCountries: List<Pair<String, String>>,
    selectedDocument: KyvshieldDocument?,
    selectedCountry: String?,
    documentDropdownExpanded: Boolean,
    onDocumentDropdownToggle: () -> Unit,
    onDocumentDropdownDismiss: () -> Unit,
    onCountrySelected: (String?) -> Unit,
    onDocumentSelected: (KyvshieldDocument?) -> Unit,
    captureSelfie: Boolean,
    captureRecto: Boolean,
    captureVerso: Boolean,
    onSelfieToggle: (Boolean) -> Unit,
    onRectoToggle: (Boolean) -> Unit,
    onVersoToggle: (Boolean) -> Unit,
    selfieDisplayMode: SelfieDisplayMode,
    documentDisplayMode: DocumentDisplayMode,
    onSelfieDisplayMode: (SelfieDisplayMode) -> Unit,
    onDocumentDisplayMode: (DocumentDisplayMode) -> Unit,
    selfieChallengeMode: ChallengeMode,
    rectoChallengeMode: ChallengeMode,
    versoChallengeMode: ChallengeMode,
    onSelfieChallengeMode: (ChallengeMode) -> Unit,
    onRectoChallengeMode: (ChallengeMode) -> Unit,
    onVersoChallengeMode: (ChallengeMode) -> Unit,
    showIntroPage: Boolean,
    showInstructionPages: Boolean,
    showResultPage: Boolean,
    showSuccessPerStep: Boolean,
    requireFaceMatch: Boolean,
    playChallengeAudio: Boolean,
    onShowIntroPage: (Boolean) -> Unit,
    onShowInstructionPages: (Boolean) -> Unit,
    onShowResultPage: (Boolean) -> Unit,
    onShowSuccessPerStep: (Boolean) -> Unit,
    onRequireFaceMatch: (Boolean) -> Unit,
    onPlayChallengeAudio: (Boolean) -> Unit,
    kycIdentifier: String,
    onKycIdentifierChange: (String) -> Unit,
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    selectedTheme: String,
    customColor: Color,
    onThemeSelected: (String) -> Unit,
    onShowColorPicker: () -> Unit,
    brightnessMode: String?,
    onBrightnessModeChange: (String?) -> Unit,
    flowSummary: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = cardColor),
        border   = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Document Type section ─────────────────────────────────────────
            SectionHeader(icon = { FileCheckIcon(primaryColor, 20.dp) }, title = t("documentType"), fonts = fonts, isLoading = isLoadingDocuments, primaryColor = primaryColor, textColor = textPrimary)
            Spacer(Modifier.height(12.dp))

            // Country filter chips
            if (availableCountries.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        CountryChip(
                            label      = t("allCountries"),
                            isSelected = selectedCountry == null,
                            primaryColor = primaryColor,
                            chipBg     = chipBg,
                            borderColor = borderColor,
                            chipText   = chipText,
                            fonts      = fonts,
                            onClick    = { onCountrySelected(null) },
                        )
                    }
                    items(availableCountries) { (code, label) ->
                        CountryChip(
                            label      = label,
                            isSelected = selectedCountry == code,
                            primaryColor = primaryColor,
                            chipBg     = chipBg,
                            borderColor = borderColor,
                            chipText   = chipText,
                            fonts      = fonts,
                            onClick    = { onCountrySelected(code) },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Document dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(inputBg)
                    .border(1.dp, inputBorder, RoundedCornerShape(12.dp))
                    .clickable { if (filteredDocuments.isNotEmpty()) onDocumentDropdownToggle() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (filteredDocuments.isEmpty()) {
                        Text(
                            text  = if (isLoadingDocuments) t("loading") else t("noDocumentAvailable"),
                            style = TextStyle(fontFamily = fonts, fontSize = 14.sp, color = textTertiary),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        val doc = selectedDocument
                        if (doc != null && filteredDocuments.contains(doc)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(primaryColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(doc.docType, style = TextStyle(fontFamily = fonts, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = primaryColor))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.name, style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = chipText), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    if (doc.hasVerso) t("rectoVerso") else t("rectoOnly"),
                                    style = TextStyle(fontFamily = fonts, fontSize = 10.sp, color = textTertiary)
                                )
                            }
                        } else {
                            Text(t("selectDocument"), style = TextStyle(fontFamily = fonts, fontSize = 14.sp, color = textTertiary), modifier = Modifier.weight(1f))
                        }
                    }
                    ChevronDownIcon(tint = primaryColor, size = 18.dp)
                }
                DropdownMenu(
                    expanded         = documentDropdownExpanded,
                    onDismissRequest = onDocumentDropdownDismiss,
                ) {
                    filteredDocuments.forEach { doc ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(primaryColor.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) { Text(doc.docType, style = TextStyle(fontFamily = fonts, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = primaryColor)) }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(doc.name, style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = chipText), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(if (doc.hasVerso) t("rectoVerso") else t("rectoOnly"), style = TextStyle(fontFamily = fonts, fontSize = 10.sp, color = textTertiary))
                                    }
                                }
                            },
                            onClick = { onDocumentSelected(doc); onDocumentDropdownDismiss() },
                        )
                    }
                }
            }

            // Verso not supported hint
            if (selectedDocument != null && !selectedDocument.hasVerso) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.08f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    InfoIcon(primaryColor, 14.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("${selectedDocument.name} ${t("rectoOnlySupported")}", style = TextStyle(fontFamily = fonts, fontSize = 11.sp, color = primaryColor))
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = borderColor)
            Spacer(Modifier.height(16.dp))

            // ── Capture Options ───────────────────────────────────────────────
            SectionHeader(icon = { CameraIcon(primaryColor, 20.dp) }, title = t("captureOptions"), fonts = fonts, primaryColor = primaryColor, textColor = textPrimary)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleChip(
                    modifier  = Modifier.weight(1f),
                    label     = t("selfie"),
                    value     = captureSelfie,
                    enabled   = true,
                    onChanged = onSelfieToggle,
                    icon      = { UserIcon(if (captureSelfie) primaryColor else textTertiary, 20.dp) },
                    primaryColor = primaryColor,
                    chipBg    = chipBg,
                    borderColor = borderColor,
                    chipText  = chipText,
                    textTertiary = textTertiary,
                    fonts     = fonts,
                )
                ToggleChip(
                    modifier  = Modifier.weight(1f),
                    label     = t("recto"),
                    value     = captureRecto,
                    enabled   = selectedDocument?.hasRecto ?: true,
                    onChanged = onRectoToggle,
                    icon      = { CreditCardIcon(if (captureRecto && (selectedDocument?.hasRecto ?: true)) primaryColor else textTertiary, 20.dp) },
                    primaryColor = primaryColor,
                    chipBg    = chipBg,
                    borderColor = borderColor,
                    chipText  = chipText,
                    textTertiary = textTertiary,
                    fonts     = fonts,
                )
                ToggleChip(
                    modifier  = Modifier.weight(1f),
                    label     = t("verso"),
                    value     = captureVerso,
                    enabled   = selectedDocument?.hasVerso ?: true,
                    onChanged = onVersoToggle,
                    icon      = { FlipHorizontalIcon(if (captureVerso && (selectedDocument?.hasVerso ?: true)) primaryColor else textTertiary, 20.dp) },
                    primaryColor = primaryColor,
                    chipBg    = chipBg,
                    borderColor = borderColor,
                    chipText  = chipText,
                    textTertiary = textTertiary,
                    fonts     = fonts,
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = borderColor)
            Spacer(Modifier.height(16.dp))

            // ── Display Mode ──────────────────────────────────────────────────
            if (captureSelfie || captureRecto || captureVerso) {
                SectionHeader(icon = { LayoutIcon(primaryColor, 20.dp) }, title = t("display"), fonts = fonts, primaryColor = primaryColor, textColor = textPrimary)
                Spacer(Modifier.height(12.dp))
                if (captureSelfie) {
                    SelfieDisplayModeRow(
                        current = selfieDisplayMode, onChanged = onSelfieDisplayMode,
                        primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor,
                        chipText = chipText, textTertiary = textTertiary, fonts = fonts, t = t,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                if (captureRecto || captureVerso) {
                    DocumentDisplayModeRow(
                        current = documentDisplayMode, onChanged = onDocumentDisplayMode,
                        primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor,
                        chipText = chipText, textTertiary = textTertiary, fonts = fonts, t = t,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                HorizontalDivider(color = borderColor)
                Spacer(Modifier.height(16.dp))
            }

            // ── Security Level ────────────────────────────────────────────────
            SectionHeader(icon = { ShieldCheckIcon(primaryColor, 20.dp) }, title = t("securityLevel"), fonts = fonts, primaryColor = primaryColor, textColor = textPrimary)
            Spacer(Modifier.height(12.dp))
            if (captureSelfie) {
                StepChallengeModeRow(
                    stepName = t("selfie"),
                    stepIcon = { UserIcon(primaryColor, 16.dp) },
                    current  = selfieChallengeMode,
                    onChanged = onSelfieChallengeMode,
                    primaryColor = primaryColor, cardColor = cardColor, inputBg = inputBg,
                    borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts,
                )
                Spacer(Modifier.height(8.dp))
            }
            if (captureRecto) {
                StepChallengeModeRow(
                    stepName = t("recto"),
                    stepIcon = { CreditCardIcon(primaryColor, 16.dp) },
                    current  = rectoChallengeMode,
                    onChanged = onRectoChallengeMode,
                    primaryColor = primaryColor, cardColor = cardColor, inputBg = inputBg,
                    borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts,
                )
                Spacer(Modifier.height(8.dp))
            }
            if (captureVerso) {
                StepChallengeModeRow(
                    stepName = t("verso"),
                    stepIcon = { FlipHorizontalIcon(primaryColor, 16.dp) },
                    current  = versoChallengeMode,
                    onChanged = onVersoChallengeMode,
                    primaryColor = primaryColor, cardColor = cardColor, inputBg = inputBg,
                    borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts,
                )
            }
            if (!captureSelfie && !captureRecto && !captureVerso) {
                Text(t("selectAtLeastOneStepAbove"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = textTertiary, fontStyle = FontStyle.Italic))
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = borderColor)
            Spacer(Modifier.height(16.dp))

            // ── Flow Options ──────────────────────────────────────────────────
            SectionHeader(icon = { LayoutListIcon(primaryColor, 20.dp) }, title = t("flowOptionsSection"), fonts = fonts, primaryColor = primaryColor, textColor = textPrimary)
            Spacer(Modifier.height(16.dp))
            SwitchTile(title = t("introPage"), subtitle = t("introPageDesc"), icon = { HomeIcon(textTertiary, 18.dp) }, value = showIntroPage, onChanged = onShowIntroPage, primaryColor = primaryColor, fonts = fonts, textPrimary = textPrimary, textTertiary = textTertiary)
            SwitchTile(title = t("instructionPages"), subtitle = t("instructionPagesDesc"), icon = { InfoIcon(textTertiary, 18.dp) }, value = showInstructionPages, onChanged = onShowInstructionPages, primaryColor = primaryColor, fonts = fonts, textPrimary = textPrimary, textTertiary = textTertiary)
            SwitchTile(title = t("resultPage"), subtitle = t("resultPageDesc"), icon = { CheckSquareIcon(textTertiary, 18.dp) }, value = showResultPage, onChanged = onShowResultPage, primaryColor = primaryColor, fonts = fonts, textPrimary = textPrimary, textTertiary = textTertiary)
            SwitchTile(title = t("successPerStep"), subtitle = t("successPerStepDesc"), icon = { SparklesIcon(textTertiary, 18.dp) }, value = showSuccessPerStep, onChanged = onShowSuccessPerStep, primaryColor = primaryColor, fonts = fonts, textPrimary = textPrimary, textTertiary = textTertiary)
            SwitchTile(title = t("faceMatch"), subtitle = t("faceMatchDesc"), icon = { ScanFaceIcon(textTertiary, 18.dp) }, value = requireFaceMatch, onChanged = onRequireFaceMatch, primaryColor = primaryColor, fonts = fonts, textPrimary = textPrimary, textTertiary = textTertiary)
            SwitchTile(title = t("challengeAudio"), subtitle = t("challengeAudioDesc"), icon = { Volume2Icon(textTertiary, 18.dp) }, value = playChallengeAudio, onChanged = onPlayChallengeAudio, primaryColor = primaryColor, fonts = fonts, textPrimary = textPrimary, textTertiary = textTertiary)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = borderColor)
            Spacer(Modifier.height(16.dp))

            // ── API Options ───────────────────────────────────────────────────
            SectionHeader(icon = { ServerIcon(primaryColor, 20.dp) }, title = t("apiOptions"), fonts = fonts, primaryColor = primaryColor, textColor = textPrimary)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value         = kycIdentifier,
                onValueChange = onKycIdentifierChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text(t("kycIdentifierHint"), style = TextStyle(fontFamily = fonts, fontSize = 14.sp, color = textTertiary)) },
                label         = { Text(t("kycIdentifier"), style = TextStyle(fontFamily = fonts, fontSize = 14.sp, color = textTertiary)) },
                leadingIcon   = { TagIcon(textTertiary, 18.dp) },
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = primaryColor,
                    unfocusedBorderColor = inputBorder,
                    focusedContainerColor = inputBg,
                    unfocusedContainerColor = inputBg,
                    focusedTextColor     = textPrimary,
                    unfocusedTextColor   = textPrimary,
                ),
                textStyle = TextStyle(fontFamily = fonts, fontSize = 14.sp, color = textPrimary),
                singleLine = true,
            )

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = borderColor)
            Spacer(Modifier.height(16.dp))

            // ── Language ──────────────────────────────────────────────────────
            SectionHeader(icon = { LanguagesIcon(primaryColor, 20.dp) }, title = t("language"), fonts = fonts, primaryColor = primaryColor, textColor = textPrimary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val langs = listOf("fr" to "Français", "en" to "English", "wo" to "Wolof")
                langs.forEach { (code, name) ->
                    val selected = selectedLanguage == code
                    val animBorder by animateColorAsState(if (selected) primaryColor else borderColor, label = "langBorder")
                    val animBg    by animateColorAsState(if (selected) primaryColor.copy(alpha = 0.1f) else chipBg, label = "langBg")
                    val borderWidth = if (selected) 2.dp else 1.dp
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(animBg)
                            .border(borderWidth, animBorder, RoundedCornerShape(12.dp))
                            .clickable { onLanguageSelected(code) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(code.uppercase(), style = TextStyle(fontFamily = fonts, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) primaryColor else chipText))
                            Spacer(Modifier.height(2.dp))
                            Text(name, style = TextStyle(fontFamily = fonts, fontSize = 10.sp, color = if (selected) primaryColor else textTertiary))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color = borderColor)
            Spacer(Modifier.height(16.dp))

            // ── Theme ─────────────────────────────────────────────────────────
            SectionHeader(icon = { PaletteIcon(primaryColor, 20.dp) }, title = t("theme"), fonts = fonts, primaryColor = primaryColor, textColor = textPrimary)
            Spacer(Modifier.height(12.dp))
            val themeNames = mapOf(
                "default" to t("themeInnolink"),
                "blue"    to t("themeBlue"),
                "green"   to t("themeGreen"),
                "purple"  to t("themePurple"),
                "kratos"  to t("themeKratos"),
                "luna"    to t("themeLuna"),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                themeColorMap.entries.forEach { (key, color) ->
                    val selected = selectedTheme == key
                    val animBg     by animateColorAsState(if (selected) color.copy(alpha = 0.15f) else chipBg, label = "themeBg$key")
                    val animBorder by animateColorAsState(if (selected) color else borderColor, label = "themeBorder$key")
                    val bw = if (selected) 2.dp else 1.dp
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(animBg)
                            .border(bw, animBorder, RoundedCornerShape(20.dp))
                            .clickable { onThemeSelected(key) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, Color.White, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(themeNames[key] ?: key, style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) color else chipText))
                    }
                }
                // Custom chip
                val isCustom = selectedTheme == "custom"
                val animCustomBg by animateColorAsState(if (isCustom) customColor.copy(alpha = 0.15f) else chipBg, label = "customBg")
                val animCustomBorder by animateColorAsState(if (isCustom) customColor else borderColor, label = "customBorder")
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(animCustomBg)
                        .border(if (isCustom) 2.dp else 1.dp, animCustomBorder, RoundedCornerShape(20.dp))
                        .clickable { onShowColorPicker() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(listOf(Color.Red, Color(0xFFFFA500), Color.Yellow, Color.Green, Color.Blue, Color.Magenta, Color.Red))
                            )
                            .border(2.dp, Color.White, CircleShape)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(t("custom"), style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = if (isCustom) FontWeight.SemiBold else FontWeight.Medium, color = if (isCustom) customColor else chipText))
                    if (isCustom) {
                        Spacer(Modifier.width(6.dp))
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(customColor))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Brightness ────────────────────────────────────────────────────
            SectionHeader(icon = { SunMoonIcon(primaryColor, 20.dp) }, title = t("themeMode"), fonts = fonts, primaryColor = primaryColor, textColor = textPrimary)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BrightnessChip(label = t("light"), icon = { SunIcon(if (brightnessMode == "light") primaryColor else textTertiary, 18.dp) }, isSelected = brightnessMode == "light", primaryColor = primaryColor, chipBg = chipBg, borderColor = borderColor, chipText = chipText, fonts = fonts, modifier = Modifier.weight(1f), onClick = { onBrightnessModeChange("light") })
                BrightnessChip(label = t("dark"),  icon = { MoonIcon(if (brightnessMode == "dark") primaryColor else textTertiary, 18.dp) },  isSelected = brightnessMode == "dark",  primaryColor = primaryColor, chipBg = chipBg, borderColor = borderColor, chipText = chipText, fonts = fonts, modifier = Modifier.weight(1f), onClick = { onBrightnessModeChange("dark") })
                BrightnessChip(label = t("auto"),  icon = { MonitorIcon(if (brightnessMode == null) primaryColor else textTertiary, 18.dp) }, isSelected = brightnessMode == null,   primaryColor = primaryColor, chipBg = chipBg, borderColor = borderColor, chipText = chipText, fonts = fonts, modifier = Modifier.weight(1f), onClick = { onBrightnessModeChange(null) })
            }

            // ── Flow summary ──────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.05f))
                    .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(t("configuredFlow"), style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primaryColor))
                    Spacer(Modifier.height(6.dp))
                    Text(flowSummary, style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = chipText))
                }
            }
        }
    }
}

// ─── Action Buttons ───────────────────────────────────────────────────────────

@Composable
fun ActionButtons(
    primaryColor: Color,
    borderColor: Color,
    textSecondary: Color,
    fonts: FontFamily,
    isLoading: Boolean,
    startLabel: String,
    startingLabel: String,
    onStart: () -> Unit,
    onHealthCheck: () -> Unit,
    onValidateKey: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick   = onStart,
            enabled   = !isLoading,
            modifier  = Modifier.fillMaxWidth().height(56.dp),
            shape     = RoundedCornerShape(16.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = primaryColor),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.width(8.dp))
            } else {
                ScanIcon(Color.White, 20.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (isLoading) startingLabel else startLabel, style = TextStyle(fontFamily = fonts, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick  = onHealthCheck,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = textSecondary),
            ) {
                HeartPulseIcon(textSecondary, 16.dp)
                Spacer(Modifier.width(6.dp))
                Text("Health Check", style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary))
            }
            OutlinedButton(
                onClick  = onValidateKey,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(12.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = textSecondary),
            ) {
                KeyIcon(textSecondary, 16.dp)
                Spacer(Modifier.width(6.dp))
                Text("Validate Key", style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textSecondary))
            }
        }
    }
}

// ─── Section header helper ────────────────────────────────────────────────────

@Composable
fun SectionHeader(icon: @Composable () -> Unit, title: String, fonts: FontFamily, primaryColor: Color, textColor: Color = Color.Unspecified, isLoading: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(8.dp))
        Text(title, style = TextStyle(fontFamily = fonts, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = textColor))
        if (isLoading) {
            Spacer(Modifier.width(8.dp))
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = primaryColor)
        }
    }
}

// ─── Country Chip ─────────────────────────────────────────────────────────────

@Composable
fun CountryChip(label: String, isSelected: Boolean, primaryColor: Color, chipBg: Color, borderColor: Color, chipText: Color, fonts: FontFamily, onClick: () -> Unit) {
    val animBg by animateColorAsState(if (isSelected) primaryColor else chipBg, label = "countryBg")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(animBg)
            .border(1.dp, if (isSelected) primaryColor else borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) Color.White else chipText))
    }
}

// ─── Toggle Chip ──────────────────────────────────────────────────────────────

@Composable
fun ToggleChip(
    modifier: Modifier = Modifier,
    label: String,
    value: Boolean,
    enabled: Boolean,
    onChanged: (Boolean) -> Unit,
    icon: @Composable () -> Unit,
    primaryColor: Color,
    chipBg: Color,
    borderColor: Color,
    chipText: Color,
    textTertiary: Color,
    fonts: FontFamily,
) {
    val isDisabled     = !enabled
    val effectiveValue = enabled && value
    val animBg    by animateColorAsState(if (isDisabled) chipBg else if (effectiveValue) primaryColor.copy(alpha = 0.1f) else chipBg, label = "toggleBg")
    val animBorder by animateColorAsState(if (isDisabled) borderColor else if (effectiveValue) primaryColor else borderColor, label = "toggleBorder")
    val borderWidth = if (effectiveValue) 2.dp else 1.dp
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(animBg)
            .border(borderWidth, animBorder, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onChanged(!value) }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(Modifier.height(6.dp))
        Text(label, style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = if (effectiveValue) FontWeight.SemiBold else FontWeight.Medium, color = if (isDisabled) textTertiary else if (effectiveValue) primaryColor else chipText))
        if (isDisabled) {
            Spacer(Modifier.height(2.dp))
            Text("N/A", style = TextStyle(fontFamily = fonts, fontSize = 8.sp, color = textTertiary))
        }
    }
}

// ─── Brightness Chip ──────────────────────────────────────────────────────────

@Composable
fun BrightnessChip(label: String, icon: @Composable () -> Unit, isSelected: Boolean, primaryColor: Color, chipBg: Color, borderColor: Color, chipText: Color, fonts: FontFamily, modifier: Modifier, onClick: () -> Unit) {
    val animBg     by animateColorAsState(if (isSelected) primaryColor.copy(alpha = 0.1f) else chipBg, label = "brightnessBg")
    val animBorder by animateColorAsState(if (isSelected) primaryColor else borderColor, label = "brightnessBorder")
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(animBg)
            .border(if (isSelected) 2.dp else 1.dp, animBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(Modifier.height(4.dp))
        Text(label, style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) primaryColor else chipText))
    }
}

// ─── Switch Tile ──────────────────────────────────────────────────────────────

@Composable
fun SwitchTile(title: String, subtitle: String, icon: @Composable () -> Unit, value: Boolean, onChanged: (Boolean) -> Unit, primaryColor: Color, fonts: FontFamily, textPrimary: Color, textTertiary: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    style = TextStyle(fontFamily = fonts, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textPrimary))
            Text(subtitle, style = TextStyle(fontFamily = fonts, fontSize = 12.sp, color = textTertiary))
        }
        Switch(checked = value, onCheckedChange = onChanged, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor))
    }
}

// ─── Step Challenge Mode Row ──────────────────────────────────────────────────

@Composable
fun StepChallengeModeRow(
    stepName: String,
    stepIcon: @Composable () -> Unit,
    current: ChallengeMode,
    onChanged: (ChallengeMode) -> Unit,
    primaryColor: Color,
    cardColor: Color,
    inputBg: Color,
    borderColor: Color,
    chipText: Color,
    textTertiary: Color,
    fonts: FontFamily,
) {
    val challenges = getChallengesForMode(stepName, current)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(inputBg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                stepIcon()
                Spacer(Modifier.width(6.dp))
                Text(stepName, style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipText))
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChallengeModeChip(label = "Min", mode = ChallengeMode.minimal, current = current, onChanged = onChanged, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, fonts = fonts, modifier = Modifier.weight(1f))
                ChallengeModeChip(label = "Std", mode = ChallengeMode.standard, current = current, onChanged = onChanged, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, fonts = fonts, modifier = Modifier.weight(1f))
                ChallengeModeChip(label = "Strict", mode = ChallengeMode.strict, current = current, onChanged = onChanged, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, fonts = fonts, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                challenges.forEach { c ->
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(primaryColor.copy(alpha = 0.1f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(getChallengeShortLabel(c), style = TextStyle(fontFamily = fonts, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = primaryColor))
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeModeChip(label: String, mode: ChallengeMode, current: ChallengeMode, onChanged: (ChallengeMode) -> Unit, primaryColor: Color, cardColor: Color, borderColor: Color, chipText: Color, fonts: FontFamily, modifier: Modifier) {
    val selected = current == mode
    val animBg     by animateColorAsState(if (selected) primaryColor.copy(alpha = 0.1f) else cardColor, label = "challengeBg$label")
    val animBorder by animateColorAsState(if (selected) primaryColor else borderColor, label = "challengeBorder$label")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(animBg)
            .border(if (selected) 2.dp else 1.dp, animBorder, RoundedCornerShape(8.dp))
            .clickable { onChanged(mode) }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = TextStyle(fontFamily = fonts, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) primaryColor else chipText))
    }
}

fun getChallengesForMode(stepName: String, mode: ChallengeMode): List<String> {
    val isDoc = stepName.lowercase() in listOf("recto", "verso", "kanam", "ginnaaw")
    return if (isDoc) when (mode) {
        ChallengeMode.minimal  -> listOf("center_document")
        ChallengeMode.standard -> listOf("center_document", "tilt_left", "tilt_right")
        ChallengeMode.strict   -> listOf("center_document", "tilt_left", "tilt_right", "tilt_forward", "tilt_back")
    } else when (mode) {
        ChallengeMode.minimal  -> listOf("center_face", "close_eyes")
        ChallengeMode.standard -> listOf("center_face", "close_eyes", "turn_left", "turn_right")
        ChallengeMode.strict   -> listOf("center_face", "close_eyes", "turn_left", "turn_right", "smile", "look_up", "look_down")
    }
}

fun getChallengeShortLabel(c: String): String = when (c) {
    "center_document" -> "Center"
    "tilt_left"       -> "↙ Left"
    "tilt_right"      -> "↗ Right"
    "tilt_forward"    -> "↑ Fwd"
    "tilt_back"       -> "↓ Back"
    "center_face"     -> "Face"
    "close_eyes"      -> "Eyes"
    "turn_left"       -> "← Turn"
    "turn_right"      -> "→ Turn"
    "smile"           -> "Smile"
    "look_up"         -> "↑ Up"
    "look_down"       -> "↓ Down"
    else              -> c
}

// ─── Selfie / Document Display Mode rows ─────────────────────────────────────

@Composable
fun SelfieDisplayModeRow(current: SelfieDisplayMode, onChanged: (SelfieDisplayMode) -> Unit, primaryColor: Color, cardColor: Color, borderColor: Color, chipText: Color, textTertiary: Color, fonts: FontFamily, t: (String) -> String) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(primaryColor.copy(alpha = 0.05f)).border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) { LayoutIcon(primaryColor, 16.dp); Spacer(Modifier.width(6.dp)); Text(t("selfieDisplay"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipText)) }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DisplayModeChip(label = t("selfieDisplayStandard"), desc = t("challengeBottom"),  icon = { LayoutTemplateIcon(if (current == SelfieDisplayMode.standard) primaryColor else textTertiary, 18.dp) }, selected = current == SelfieDisplayMode.standard, onClick = { onChanged(SelfieDisplayMode.standard) },  primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts, modifier = Modifier.weight(1f))
                DisplayModeChip(label = t("selfieDisplayCompact"),  desc = t("challengeOnCamera"),icon = { LayoutDashboardIcon(if (current == SelfieDisplayMode.compact) primaryColor else textTertiary, 18.dp) }, selected = current == SelfieDisplayMode.compact, onClick = { onChanged(SelfieDisplayMode.compact) },   primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts, modifier = Modifier.weight(1f))
                DisplayModeChip(label = t("selfieDisplayImmersive"),desc = t("fullScreen"),        icon = { Maximize2Icon(if (current == SelfieDisplayMode.immersive) primaryColor else textTertiary, 18.dp) },    selected = current == SelfieDisplayMode.immersive, onClick = { onChanged(SelfieDisplayMode.immersive) }, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts, modifier = Modifier.weight(1f))
                DisplayModeChip(label = "Neon HUD",                 desc = "Futuriste",           icon = { MonitorIcon(if (current == SelfieDisplayMode.neonHud) primaryColor else textTertiary, 18.dp) },         selected = current == SelfieDisplayMode.neonHud,   onClick = { onChanged(SelfieDisplayMode.neonHud) },   primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DocumentDisplayModeRow(current: DocumentDisplayMode, onChanged: (DocumentDisplayMode) -> Unit, primaryColor: Color, cardColor: Color, borderColor: Color, chipText: Color, textTertiary: Color, fonts: FontFamily, t: (String) -> String) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(primaryColor.copy(alpha = 0.05f)).border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) { CreditCardIcon(primaryColor, 16.dp); Spacer(Modifier.width(8.dp)); Text(t("documentDisplay"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipText)) }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DisplayModeChip(label = t("docDisplayStandard"),  desc = t("instructionsBottom"),  icon = { LayoutTemplateIcon(if (current == DocumentDisplayMode.standard) primaryColor else textTertiary, 18.dp) }, selected = current == DocumentDisplayMode.standard,  onClick = { onChanged(DocumentDisplayMode.standard) },  primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts, modifier = Modifier.weight(1f))
                DisplayModeChip(label = t("docDisplayCompact"),   desc = t("instructionsOnCamera"),icon = { LayoutDashboardIcon(if (current == DocumentDisplayMode.compact) primaryColor else textTertiary, 18.dp) },  selected = current == DocumentDisplayMode.compact,   onClick = { onChanged(DocumentDisplayMode.compact) },   primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts, modifier = Modifier.weight(1f))
                DisplayModeChip(label = t("docDisplayImmersive"), desc = t("fullScreen"),          icon = { Maximize2Icon(if (current == DocumentDisplayMode.immersive) primaryColor else textTertiary, 18.dp) },     selected = current == DocumentDisplayMode.immersive, onClick = { onChanged(DocumentDisplayMode.immersive) }, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts, modifier = Modifier.weight(1f))
                DisplayModeChip(label = "Neon HUD",               desc = "Futuriste",             icon = { MonitorIcon(if (current == DocumentDisplayMode.neonHud) primaryColor else textTertiary, 18.dp) },          selected = current == DocumentDisplayMode.neonHud,   onClick = { onChanged(DocumentDisplayMode.neonHud) },   primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textTertiary = textTertiary, fonts = fonts, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DisplayModeChip(label: String, desc: String, icon: @Composable () -> Unit, selected: Boolean, onClick: () -> Unit, primaryColor: Color, cardColor: Color, borderColor: Color, chipText: Color, textTertiary: Color, fonts: FontFamily, modifier: Modifier) {
    val animBg     by animateColorAsState(if (selected) primaryColor.copy(alpha = 0.1f) else cardColor, label = "dispBg$label")
    val animBorder by animateColorAsState(if (selected) primaryColor else borderColor, label = "dispBorder$label")
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(animBg)
            .border(if (selected) 2.dp else 1.dp, animBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon()
        Spacer(Modifier.height(4.dp))
        Text(label, style = TextStyle(fontFamily = fonts, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) primaryColor else chipText), textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(desc, style = TextStyle(fontFamily = fonts, fontSize = 8.sp, color = if (selected) primaryColor.copy(alpha = 0.7f) else textTertiary), textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ─── Color Picker Dialog ──────────────────────────────────────────────────────

@Composable
fun ColorPickerDialog(
    currentColor: Color,
    titleText: String,
    cancelText: String,
    applyText: String,
    onDismiss: () -> Unit,
    onApply: (Color) -> Unit,
    fonts: FontFamily,
    cardColor: Color,
    textPrimary: Color,
) {
    var tempColor by remember { mutableStateOf(currentColor) }
    val presetColors = listOf(
        Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFEF8352), Color(0xFFF59E0B), Color(0xFFEAB308),
        Color(0xFF84CC16), Color(0xFF22C55E), Color(0xFF10B981), Color(0xFF14B8A6), Color(0xFF06B6D4),
        Color(0xFF0EA5E9), Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFA855F7),
        Color(0xFFEC4899), Color(0xFFF43F5E), Color(0xFF00377D), Color(0xFFFFD100), Color(0xFF64748B),
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.9f), shape = RoundedCornerShape(16.dp), color = cardColor) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(titleText, style = TextStyle(fontFamily = fonts, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = textPrimary))
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(tempColor).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { tempColor = color }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(cancelText, fontFamily = fonts) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onApply(tempColor) }) { Text(applyText, fontFamily = fonts) }
                }
            }
        }
    }
}

// ─── Image Popup Dialog ───────────────────────────────────────────────────────

@Composable
fun ImagePopupDialog(imageBytes: ByteArray, title: String, onDismiss: () -> Unit, onSave: () -> Unit, chipText: Color, textPrimary: Color) {
    val bitmap = remember(imageBytes) { BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap() }
    var scale  by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.87f))
                .pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(0.5f, 5f); offset += pan } }
        ) {
            bitmap?.let {
                Image(
                    bitmap             = it,
                    contentDescription = title,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                )
            }
            // Title bar at bottom
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(30.dp)).background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) { Text(title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))) }
            // Download button top-left
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
                    .size(44.dp).clip(CircleShape).background(Color.White)
                    .clickable(onClick = onSave),
                contentAlignment = Alignment.Center,
            ) { DownloadIcon(chipText, 20.dp) }
            // Close button top-right
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                    .size(44.dp).clip(CircleShape).background(Color.White)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) { XIcon(chipText, 20.dp) }
        }
    }
}

// ─── Result Section ───────────────────────────────────────────────────────────

@Composable
fun ResultSection(
    result: KYCResult,
    primaryColor: Color,
    cardColor: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    textTertiary: Color,
    inputBg: Color,
    inputBorder: Color,
    chipBg: Color,
    chipText: Color,
    fonts: FontFamily,
    monoFonts: FontFamily,
    requireFaceMatch: Boolean,
    t: (String) -> String,
    onImageClick: (ByteArray, String) -> Unit,
    onCopySessionId: (String) -> Unit,
) {
    val hasAnalysis = result.rectoResult != null || result.versoResult != null
    val isSuccess: Boolean = if (hasAnalysis) {
        val rectoOk = result.rectoResult?.success ?: true
        val versoOk = result.versoResult?.success ?: true
        val faceOk  = result.faceResult?.success ?: result.rectoResult?.faceVerification?.success ?: true
        rectoOk && versoOk && faceOk
    } else result.selfieImageBytes != null

    val statusColor = if (isSuccess) primaryColor else Color.Red
    val resultTitle = getResultTitle(result, t)

    Column {
        // Main Result Card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.05f)),
            border    = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSuccess) CheckCircleIcon(statusColor, 24.dp) else XCircleIcon(statusColor, 24.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(resultTitle, style = TextStyle(fontFamily = fonts, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = statusColor), modifier = Modifier.weight(1f))
                    if (hasAnalysis) {
                        val sc = getStatusColor(result.overallStatus, primaryColor)
                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(sc.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(result.overallStatus.name.uppercase(), style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = sc))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HashIcon(textTertiary, 14.dp)
                    Spacer(Modifier.width(4.dp))
                    Text("Session: ${result.sessionId ?: "N/A"}", style = TextStyle(fontFamily = monoFonts, fontSize = 11.sp, color = textSecondary), modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis, maxLines = 1)
                    result.sessionId?.let { sid ->
                        IconButton(onClick = { onCopySessionId(sid) }, modifier = Modifier.size(24.dp)) {
                            CopyIcon(textTertiary, 14.dp)
                        }
                    }
                }
                if (hasAnalysis) {
                    Spacer(Modifier.height(16.dp))
                    AuthenticityScoreWidget(result = result, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, textPrimary = textPrimary, textSecondary = textSecondary, fonts = fonts, t = t)
                }
                if (!hasAnalysis && result.selfieImageBytes != null) {
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(primaryColor.copy(alpha = 0.08f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        InfoIcon(primaryColor, 18.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(t("selfieCapturedNoAnalysis"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = primaryColor), modifier = Modifier.weight(1f))
                    }
                }
                result.errorMessage?.let { err ->
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.Red.copy(alpha = 0.1f)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AlertCircleIcon(Color.Red, 18.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(err, style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = Color(0xFFB91C1C)), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Document Images
        if (result.rectoImageBytes != null || result.versoImageBytes != null || result.selfieImageBytes != null) {
            Spacer(Modifier.height(16.dp))
            DocumentImagesCard(result = result, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipBg = chipBg, chipText = chipText, textPrimary = textPrimary, textSecondary = textSecondary, textTertiary = textTertiary, fonts = fonts, t = t, onImageClick = onImageClick)
        }

        // Recto extraction
        result.rectoExtraction?.let { data ->
            Spacer(Modifier.height(16.dp))
            StepExtractedDataCard(stepName = "RECTO", docData = data, stepColor = primaryColor, cardColor = cardColor, borderColor = borderColor, inputBg = inputBg, inputBorder = inputBorder, chipBg = chipBg, chipText = chipText, textTertiary = textTertiary, textPrimary = textPrimary, fonts = fonts, monoFonts = monoFonts, t = t)
        } ?: run {
            if (result.rectoResult != null) {
                Spacer(Modifier.height(16.dp))
                StepExtractedDataCard(stepName = "RECTO", docData = null, stepColor = primaryColor, cardColor = cardColor, borderColor = borderColor, inputBg = inputBg, inputBorder = inputBorder, chipBg = chipBg, chipText = chipText, textTertiary = textTertiary, textPrimary = textPrimary, fonts = fonts, monoFonts = monoFonts, t = t)
            }
        }

        // Verso extraction
        result.versoExtraction?.let { data ->
            Spacer(Modifier.height(16.dp))
            StepExtractedDataCard(stepName = "VERSO", docData = data, stepColor = primaryColor, cardColor = cardColor, borderColor = borderColor, inputBg = inputBg, inputBorder = inputBorder, chipBg = chipBg, chipText = chipText, textTertiary = textTertiary, textPrimary = textPrimary, fonts = fonts, monoFonts = monoFonts, t = t)
        } ?: run {
            if (result.versoResult != null) {
                Spacer(Modifier.height(16.dp))
                StepExtractedDataCard(stepName = "VERSO", docData = null, stepColor = primaryColor, cardColor = cardColor, borderColor = borderColor, inputBg = inputBg, inputBorder = inputBorder, chipBg = chipBg, chipText = chipText, textTertiary = textTertiary, textPrimary = textPrimary, fonts = fonts, monoFonts = monoFonts, t = t)
            }
        }

        // Face verification
        if (requireFaceMatch && (result.faceResult != null || result.rectoResult?.faceVerification != null)) {
            Spacer(Modifier.height(16.dp))
            FaceVerificationCard(result = result, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, inputBg = inputBg, chipBg = chipBg, chipText = chipText, textPrimary = textPrimary, textSecondary = textSecondary, fonts = fonts, monoFonts = monoFonts, t = t)
        }

        // AML Screening
        result.amlScreening?.let { aml ->
            Spacer(Modifier.height(16.dp))
            AmlScreeningCard(aml = aml, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, textPrimary = textPrimary, textSecondary = textSecondary, fonts = fonts, t = t)
        }

        // Component scores
        if (result.rectoResult != null || result.versoResult != null) {
            Spacer(Modifier.height(16.dp))
            ComponentScoresCard(result = result, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, chipText = chipText, textPrimary = textPrimary, fonts = fonts, t = t)
        }

        // Processing times
        if (hasAnalysis) {
            Spacer(Modifier.height(16.dp))
            ProcessingTimesCard(result = result, primaryColor = primaryColor, cardColor = cardColor, borderColor = borderColor, textPrimary = textPrimary, fonts = fonts, t = t)
        }
    }
}

@Composable
fun AmlScreeningCard(aml: AMLScreening, primaryColor: Color, cardColor: Color, borderColor: Color, textPrimary: Color, textSecondary: Color, fonts: FontFamily, t: (String) -> String) {
    val isClear = aml.status == "clear"
    val isMatch = aml.status == "match"
    val color = if (isClear) primaryColor else if (isMatch) Color.Red else Color(0xFFF97316)
    val statusLabel = when (aml.status) {
        "clear" -> t("amlClear")
        "match" -> t("amlMatch")
        "error" -> t("amlError")
        else    -> t("amlDisabled")
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShieldCheckIcon(color, 20.dp)
                Spacer(Modifier.width(8.dp))
                Text(t("amlScreening"), style = TextStyle(fontFamily = fonts, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = color), modifier = Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(cardColor).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(aml.status.uppercase(), style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("amlStatus"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = textSecondary))
                Text(statusLabel, style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color))
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t("amlRiskLevel"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = textSecondary))
                Text(aml.riskLevel.uppercase(), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color))
            }
            if (aml.totalMatches > 0) {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(t("amlMatches"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, color = textSecondary))
                    Text("${aml.totalMatches}", style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Red))
                }
            }
        }
    }
}

fun getResultTitle(result: KYCResult, t: (String) -> String): String {
    val hasRecto     = result.rectoResult != null
    val hasVerso     = result.versoResult != null
    val hasSelfie    = result.selfieImageBytes != null
    val hasFaceMatch = result.faceResult != null || result.rectoResult?.faceVerification != null
    val rectoSuccess = result.rectoResult?.success ?: false
    val versoSuccess = result.versoResult?.success ?: false
    val selfieSuccess = result.selfieResult?.success ?: false
    val faceSuccess  = result.faceResult?.success ?: result.rectoResult?.faceVerification?.success ?: false
    return when {
        hasRecto && hasVerso && hasFaceMatch -> if (rectoSuccess && versoSuccess && faceSuccess) t("verificationCompleteSuccess") else t("verificationFailed")
        hasRecto && hasVerso                 -> if (rectoSuccess && versoSuccess) t("documentsVerified") else t("documentVerificationFailed")
        hasRecto && hasFaceMatch             -> if (rectoSuccess && faceSuccess) t("rectoFaceVerified") else t("verificationFailed")
        hasRecto                             -> if (rectoSuccess) t("rectoVerified") else t("rectoVerificationFailed")
        hasVerso                             -> if (versoSuccess) t("versoVerified") else t("versoVerificationFailed")
        hasSelfie                            -> if (selfieSuccess) t("selfieCaptured") else t("selfieFailed")
        else                                 -> if (result.success) t("completed") else t("cancelled")
    }
}

fun getStatusColor(status: VerificationStatus, primaryColor: Color): Color = when (status) {
    VerificationStatus.pass   -> primaryColor
    VerificationStatus.review -> Color(0xFFF97316)
    VerificationStatus.reject -> Color.Red
    VerificationStatus.error  -> Color.Gray
}

// ─── Authenticity Score ───────────────────────────────────────────────────────

@Composable
fun AuthenticityScoreWidget(result: KYCResult, primaryColor: Color, cardColor: Color, borderColor: Color, textPrimary: Color, textSecondary: Color, fonts: FontFamily, t: (String) -> String) {
    val score = (result.authenticityScore * 100).toInt()
    val color = if (score >= 70) primaryColor else if (score >= 40) Color(0xFFF97316) else Color.Red
    val scoreDetails = buildList {
        result.rectoResult?.let { add("${t("recto")}: ${((result.rectoAuthenticityScore ?: 0.0) * 100).toInt()}%") }
        result.versoResult?.let { add("${t("verso")}: ${((result.versoAuthenticityScore ?: 0.0) * 100).toInt()}%") }
    }
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(cardColor).border(1.dp, borderColor, RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(70.dp), contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(70.dp)) {
                val strokeWidth = 5.dp.toPx()
                val inset = strokeWidth / 2
                drawArc(color = borderColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(strokeWidth), topLeft = Offset(inset, inset), size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth))
                drawArc(color = color, startAngle = -90f, sweepAngle = 360f * result.authenticityScore.toFloat().coerceIn(0f, 1f), useCenter = false, style = Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round), topLeft = Offset(inset, inset), size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth))
            }
            Text("$score%", style = TextStyle(fontFamily = fonts, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(t("authenticityScore"), style = TextStyle(fontFamily = fonts, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary))
            if (scoreDetails.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(scoreDetails.joinToString(" • "), style = TextStyle(fontFamily = fonts, fontSize = 12.sp, color = textSecondary))
            }
        }
    }
}

// ─── Document Images Card ─────────────────────────────────────────────────────

@Composable
fun DocumentImagesCard(result: KYCResult, primaryColor: Color, cardColor: Color, borderColor: Color, chipBg: Color, chipText: Color, textPrimary: Color, textSecondary: Color, textTertiary: Color, fonts: FontFamily, t: (String) -> String, onImageClick: (ByteArray, String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { CreditCardIcon(primaryColor, 18.dp); Spacer(Modifier.width(8.dp)); Text(t("scannedDocuments"), style = TextStyle(fontFamily = fonts, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)) }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                result.rectoImageBytes?.let { bytes ->
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.53f).clip(RoundedCornerShape(8.dp)).clickable { onImageClick(bytes, "${t("document")} ${t("recto")}") }) {
                            Image(bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap(), contentDescription = t("recto"), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(4.dp)) { Maximize2Icon(Color.White, 14.dp) }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(t("recto"), style = TextStyle(fontFamily = fonts, fontSize = 12.sp, color = textTertiary), modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
                result.versoImageBytes?.let { bytes ->
                    Column(modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.53f).clip(RoundedCornerShape(8.dp)).clickable { onImageClick(bytes, "${t("document")} ${t("verso")}") }) {
                            Image(bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap(), contentDescription = t("verso"), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(4.dp)) { Maximize2Icon(Color.White, 14.dp) }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(t("verso"), style = TextStyle(fontFamily = fonts, fontSize = 12.sp, color = textTertiary), modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }
            result.selfieImageBytes?.let { bytes ->
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = borderColor)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { UserIcon(textSecondary, 16.dp); Spacer(Modifier.width(8.dp)); Text(t("selfie"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipText)) }
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp)).border(2.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).clickable { onImageClick(bytes, t("selfie")) }) {
                        Image(bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap(), contentDescription = t("selfie"), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).clip(RoundedCornerShape(4.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(4.dp)) { Maximize2Icon(Color.White, 12.dp) }
                    }
                }
            }
            if (result.hasExtractedPhotos) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = borderColor)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { ScanIcon(primaryColor, 16.dp); Spacer(Modifier.width(8.dp)); Text("${t("extractedPhotos")} (${result.allExtractedPhotos.size})", style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipText)) }
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    result.allExtractedPhotos.forEachIndexed { idx, photo ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.width(80.dp).height(100.dp).clip(RoundedCornerShape(8.dp)).border(2.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).clickable { onImageClick(photo.imageBytes, "${t("extractedPhoto")} ${idx + 1}") }) {
                                if (photo.imageBytes.isNotEmpty()) {
                                    val bmp = remember(photo.imageBytes) { BitmapFactory.decodeByteArray(photo.imageBytes, 0, photo.imageBytes.size)?.asImageBitmap() }
                                    bmp?.let { Image(bitmap = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                        ?: Box(modifier = Modifier.fillMaxSize().background(chipBg), contentAlignment = Alignment.Center) { ImageOffIcon(textTertiary, 24.dp) }
                                }
                                Box(modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).clip(RoundedCornerShape(3.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(3.dp)) { Maximize2Icon(Color.White, 10.dp) }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${photo.width}×${photo.height}", style = TextStyle(fontFamily = fonts, fontSize = 10.sp, color = textTertiary))
                            Text("${(photo.confidence * 100).toInt()}% ${t("humanFace")}", style = TextStyle(fontFamily = fonts, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = primaryColor))
                        }
                    }
                }
            }
        }
    }
}

// ─── Step Extracted Data Card ─────────────────────────────────────────────────

@Composable
fun StepExtractedDataCard(stepName: String, docData: DocumentData?, stepColor: Color, cardColor: Color, borderColor: Color, inputBg: Color, inputBorder: Color, chipBg: Color, chipText: Color, textTertiary: Color, textPrimary: Color, fonts: FontFamily, monoFonts: FontFamily, t: (String) -> String) {
    if (docData == null) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                FileTextIcon(stepColor, 18.dp); Spacer(Modifier.width(8.dp))
                Text("$stepName - ${t("noExtractedData")}", style = TextStyle(fontFamily = fonts, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textTertiary))
            }
        }
        return
    }
    val displayFields = docData.sortedFields.filter { it.stringValue?.isNotEmpty() == true && !it.key.startsWith("mrz_line") }
    val mrzFields     = docData.sortedFields.filter { it.key.startsWith("mrz_line") && it.stringValue?.isNotEmpty() == true }
    if (displayFields.isEmpty() && mrzFields.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                FileTextIcon(stepColor, 18.dp); Spacer(Modifier.width(8.dp))
                Text("$stepName - ${t("noExtractedData")}", style = TextStyle(fontFamily = fonts, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textTertiary))
            }
        }
        return
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, stepColor.copy(alpha = 0.3f)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (stepName == "RECTO") CreditCardIcon(stepColor, 18.dp) else FlipHorizontalIcon(stepColor, 18.dp)
                Spacer(Modifier.width(8.dp))
                Text("$stepName (${displayFields.size} ${t("fields")})", style = TextStyle(fontFamily = fonts, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = stepColor))
            }
            Spacer(Modifier.height(12.dp))
            displayFields.forEach { field ->
                DataTile(label = field.label.ifEmpty { field.key }, value = field.stringValue ?: "", icon = { getIconForField(field.key, textTertiary, 12.dp) }, inputBg = inputBg, borderColor = borderColor, fonts = fonts, textPrimary = textPrimary, textTertiary = textTertiary)
                Spacer(Modifier.height(6.dp))
            }
            if (mrzFields.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = borderColor)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { ScanLineIcon(textTertiary, 16.dp); Spacer(Modifier.width(8.dp)); Text(t("mrz"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipText)) }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(chipBg).border(1.dp, inputBorder, RoundedCornerShape(8.dp)).padding(12.dp)) {
                    Text(mrzFields.joinToString("\n") { it.stringValue ?: "" }, style = TextStyle(fontFamily = monoFonts, fontSize = 11.sp, color = textPrimary, lineHeight = 16.sp))
                }
            }
        }
    }
}

@Composable
fun DataTile(label: String, value: String, icon: @Composable () -> Unit, inputBg: Color, borderColor: Color, fonts: FontFamily, textPrimary: Color, textTertiary: Color) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(inputBg).border(1.dp, borderColor, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) { icon(); Spacer(Modifier.width(6.dp)); Text(label, style = TextStyle(fontFamily = fonts, fontSize = 10.sp, color = textTertiary), overflow = TextOverflow.Ellipsis, maxLines = 1) }
            Spacer(Modifier.height(4.dp))
            Text(value, style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimary), overflow = TextOverflow.Ellipsis, maxLines = 2)
        }
    }
}

@Composable
fun getIconForField(key: String, tint: Color, size: Dp) {
    when (key) {
        "document_id", "document_number", "cin" -> CreditCardIcon(tint, size)
        "national_id", "nin"                    -> FingerprintIcon(tint, size)
        "last_name", "first_name", "first_names", "sex" -> UserIcon(tint, size)
        "birth_date", "date_of_birth", "issue_date", "expiry_date" -> CalendarIcon(tint, size)
        "birth_place", "place_of_birth", "birth_region", "address" -> MapPinIcon(tint, size)
        "profession"                            -> BriefcaseIcon(tint, size)
        "nationality", "country_code"           -> GlobeIcon(tint, size)
        "height"                                -> RulerIcon(tint, size)
        "issuing_authority", "electoral_status" -> BuildingIcon(tint, size)
        else                                    -> FileTextIcon(tint, size)
    }
}

// ─── Face Verification Card ───────────────────────────────────────────────────

@Composable
fun FaceVerificationCard(result: KYCResult, primaryColor: Color, cardColor: Color, borderColor: Color, inputBg: Color, chipBg: Color, chipText: Color, textPrimary: Color, textSecondary: Color, fonts: FontFamily, monoFonts: FontFamily, t: (String) -> String) {
    val fv = result.faceResult ?: result.rectoResult?.faceVerification ?: return
    val isMatch    = fv.isMatch
    val similarity = (fv.similarityScore * 100).toInt()
    val color      = if (isMatch) primaryColor else Color.Red
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)), border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isMatch) UserCheckIcon(color, 20.dp) else UserXIcon(color, 20.dp)
                Spacer(Modifier.width(8.dp))
                Text(if (isMatch) t("faceMatchResult") else t("faceNoMatchResult"), style = TextStyle(fontFamily = fonts, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = color), modifier = Modifier.weight(1f))
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(cardColor).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("$similarity%", style = TextStyle(fontFamily = fonts, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color))
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FaceDetectionTile("CIN Face", fv.cinFaceDetected, fv.cinFaceConfidence, primaryColor, inputBg, textSecondary, textPrimary, fonts, t, Modifier.weight(1f))
                FaceDetectionTile("Selfie Face", fv.selfieFaceDetected, fv.selfieFaceConfidence, primaryColor, inputBg, textSecondary, textPrimary, fonts, t, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ModelTag("Detection: ${fv.detectionModel}", chipBg, chipText, monoFonts)
                ModelTag("Recognition: ${fv.recognitionModel}", chipBg, chipText, monoFonts)
                ModelTag("${t("threshold")}: ${(fv.threshold * 100).toInt()}%", chipBg, chipText, monoFonts)
            }
        }
    }
}

@Composable
fun FaceDetectionTile(label: String, detected: Boolean, confidence: Double, primaryColor: Color, inputBg: Color, textSecondary: Color, textPrimary: Color, fonts: FontFamily, t: (String) -> String, modifier: Modifier) {
    Row(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(inputBg).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (detected) CheckCircleIcon(primaryColor, 16.dp) else XCircleIcon(Color.Red, 16.dp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = TextStyle(fontFamily = fonts, fontSize = 11.sp, color = textSecondary), overflow = TextOverflow.Ellipsis, maxLines = 1)
            Text(if (detected) "${(confidence * 100).toInt()}%" else t("notDetected"), style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textPrimary))
        }
    }
}

@Composable
fun ModelTag(text: String, chipBg: Color, chipText: Color, monoFonts: FontFamily) {
    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(chipBg).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(text, style = TextStyle(fontFamily = monoFonts, fontSize = 10.sp, color = chipText))
    }
}

// ─── Component Scores Card ────────────────────────────────────────────────────

@Composable
fun ComponentScoresCard(result: KYCResult, primaryColor: Color, cardColor: Color, borderColor: Color, chipText: Color, textPrimary: Color, fonts: FontFamily, t: (String) -> String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { BarChart3Icon(primaryColor, 18.dp); Spacer(Modifier.width(8.dp)); Text(t("componentScores"), style = TextStyle(fontFamily = fonts, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)) }
            Spacer(Modifier.height(16.dp))
            result.rectoResult?.let { r ->
                Text(t("recto"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipText))
                Spacer(Modifier.height(8.dp))
                ComponentScoreRows(scores = r.fraudAnalysis.componentScores, primaryColor = primaryColor, borderColor = borderColor, chipText = chipText, fonts = fonts, t = t)
            }
            result.versoResult?.let { v ->
                Spacer(Modifier.height(16.dp))
                Text(t("verso"), style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = chipText))
                Spacer(Modifier.height(8.dp))
                ComponentScoreRows(scores = v.fraudAnalysis.componentScores, primaryColor = primaryColor, borderColor = borderColor, chipText = chipText, fonts = fonts, t = t)
            }
        }
    }
}

@Composable
fun ComponentScoreRows(scores: Map<String, Double>, primaryColor: Color, borderColor: Color, chipText: Color, fonts: FontFamily, t: (String) -> String) {
    val components = listOf("overall" to t("overallScore"), "liveness" to t("liveness"))
    components.filter { scores.containsKey(it.first) }.forEach { (key, label) ->
        val raw   = scores[key]!!
        val pct   = (raw * 100).toInt()
        val color = if (pct >= 70) primaryColor else if (pct >= 40) Color(0xFFF97316) else Color.Red
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.1f)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(10.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = TextStyle(fontFamily = fonts, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = chipText))
                    Text("$pct%", style = TextStyle(fontFamily = fonts, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color))
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { raw.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color    = color,
                    trackColor = borderColor,
                )
            }
        }
    }
}

// ─── Processing Times Card ────────────────────────────────────────────────────

@Composable
fun ProcessingTimesCard(result: KYCResult, primaryColor: Color, cardColor: Color, borderColor: Color, textPrimary: Color, fonts: FontFamily, t: (String) -> String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { ClockIcon(primaryColor, 18.dp); Spacer(Modifier.width(8.dp)); Text(t("processingTime"), style = TextStyle(fontFamily = fonts, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)) }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeTile(label = t("total"), ms = result.totalProcessingTimeMs, primaryColor = primaryColor, fonts = fonts, modifier = Modifier.weight(1f))
                result.rectoResult?.let { TimeTile(label = t("recto"), ms = it.processingTimeMs, primaryColor = primaryColor, fonts = fonts, modifier = Modifier.weight(1f)) }
                result.versoResult?.let { TimeTile(label = t("verso"), ms = it.processingTimeMs, primaryColor = primaryColor, fonts = fonts, modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun TimeTile(label: String, ms: Int, primaryColor: Color, fonts: FontFamily, modifier: Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(primaryColor.copy(alpha = 0.1f)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${"%.1f".format(ms / 1000.0)}s", style = TextStyle(fontFamily = fonts, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryColor))
        Text(label, style = TextStyle(fontFamily = fonts, fontSize = 11.sp, color = primaryColor.copy(alpha = 0.7f)))
    }
}

// ─── Icon helpers (Canvas-drawn, matching Lucide shapes) ─────────────────────
// Each function draws a simple recognizable icon using Canvas paths.

@Composable fun ShieldIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.5f, s*.05f); lineTo(s*.9f, s*.2f); lineTo(s*.9f, s*.55f)
        cubicTo(s*.9f, s*.8f, s*.5f, s*.95f, s*.5f, s*.95f)
        cubicTo(s*.5f, s*.95f, s*.1f, s*.8f, s*.1f, s*.55f)
        lineTo(s*.1f, s*.2f); close()
    }
    drawPath(p, tint, style = Stroke(s * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun ShieldCheckIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val shield = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.5f, s*.05f); lineTo(s*.9f, s*.2f); lineTo(s*.9f, s*.55f)
        cubicTo(s*.9f, s*.8f, s*.5f, s*.95f, s*.5f, s*.95f)
        cubicTo(s*.5f, s*.95f, s*.1f, s*.8f, s*.1f, s*.55f)
        lineTo(s*.1f, s*.2f); close()
    }
    drawPath(shield, tint, style = Stroke(s * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    val tick = androidx.compose.ui.graphics.Path().apply { moveTo(s*.35f, s*.5f); lineTo(s*.47f, s*.62f); lineTo(s*.65f, s*.40f) }
    drawPath(tick, tint, style = Stroke(s * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun CheckCircleIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.42f, style = Stroke(s * 0.07f))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.33f, s*.5f); lineTo(s*.45f, s*.63f); lineTo(s*.67f, s*.38f) }
    drawPath(p, tint, style = Stroke(s * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun XCircleIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.42f, style = Stroke(s * 0.07f))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.35f, s*.35f); lineTo(s*.65f, s*.65f); moveTo(s*.65f, s*.35f); lineTo(s*.35f, s*.65f) }
    drawPath(p, tint, style = Stroke(s * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun CreditCardIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.08f, s*.25f), size = androidx.compose.ui.geometry.Size(s*.84f, s*.5f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
    drawLine(tint, Offset(s*.08f, s*.42f), Offset(s*.92f, s*.42f), s*.07f)
    drawLine(tint, Offset(s*.15f, s*.58f), Offset(s*.35f, s*.58f), s*.07f)
}
@Composable fun UserIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.18f, center = Offset(s*.5f, s*.33f), style = Stroke(s*.07f))
    drawArc(tint, 180f, 180f, false, topLeft = Offset(s*.2f, s*.55f), size = androidx.compose.ui.geometry.Size(s*.6f, s*.4f), style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun FlipHorizontalIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.5f, s*.15f); lineTo(s*.5f, s*.85f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(s*.1f, s*.06f))))
    val l = androidx.compose.ui.graphics.Path().apply { moveTo(s*.5f, s*.3f); lineTo(s*.15f, s*.5f); lineTo(s*.5f, s*.7f) }
    val r = androidx.compose.ui.graphics.Path().apply { moveTo(s*.5f, s*.3f); lineTo(s*.85f, s*.5f); lineTo(s*.5f, s*.7f) }
    drawPath(l, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawPath(r, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun CameraIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.07f, s*.3f), size = androidx.compose.ui.geometry.Size(s*.86f, s*.55f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.08f), style = Stroke(s*.07f))
    drawCircle(tint, s*.14f, center = Offset(s*.5f, s*.57f), style = Stroke(s*.07f))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.35f, s*.3f); lineTo(s*.42f, s*.18f); lineTo(s*.58f, s*.18f); lineTo(s*.65f, s*.3f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun LayoutIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.1f), size = androidx.compose.ui.geometry.Size(s*.8f, s*.8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
    drawLine(tint, Offset(s*.1f, s*.4f), Offset(s*.9f, s*.4f), s*.07f)
    drawLine(tint, Offset(s*.45f, s*.4f), Offset(s*.45f, s*.9f), s*.07f)
}
@Composable fun LayoutListIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    listOf(s*.25f, s*.5f, s*.75f).forEach { y ->
        drawLine(tint, Offset(s*.1f, y), Offset(s*.4f, y), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(tint, Offset(s*.5f, y), Offset(s*.9f, y), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}
@Composable fun LayoutTemplateIcon(tint: Color, size: Dp) = LayoutIcon(tint, size)
@Composable fun LayoutDashboardIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.1f), size = androidx.compose.ui.geometry.Size(s*.35f, s*.35f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.05f), style = Stroke(s*.07f))
    drawRoundRect(tint, topLeft = Offset(s*.55f, s*.1f), size = androidx.compose.ui.geometry.Size(s*.35f, s*.35f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.05f), style = Stroke(s*.07f))
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.55f), size = androidx.compose.ui.geometry.Size(s*.35f, s*.35f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.05f), style = Stroke(s*.07f))
    drawRoundRect(tint, topLeft = Offset(s*.55f, s*.55f), size = androidx.compose.ui.geometry.Size(s*.35f, s*.35f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.05f), style = Stroke(s*.07f))
}
@Composable fun Maximize2Icon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.6f, s*.1f); lineTo(s*.9f, s*.1f); lineTo(s*.9f, s*.4f)
        moveTo(s*.9f, s*.1f); lineTo(s*.55f, s*.45f)
        moveTo(s*.4f, s*.9f); lineTo(s*.1f, s*.9f); lineTo(s*.1f, s*.6f)
        moveTo(s*.1f, s*.9f); lineTo(s*.45f, s*.55f)
    }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun MonitorIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.05f, s*.12f), size = androidx.compose.ui.geometry.Size(s*.9f, s*.6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
    drawLine(tint, Offset(s*.35f, s*.72f), Offset(s*.35f, s*.88f), s*.07f)
    drawLine(tint, Offset(s*.65f, s*.72f), Offset(s*.65f, s*.88f), s*.07f)
    drawLine(tint, Offset(s*.25f, s*.88f), Offset(s*.75f, s*.88f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun FileCheckIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.6f, s*.07f); lineTo(s*.2f, s*.07f); cubicTo(s*.15f, s*.07f, s*.1f, s*.12f, s*.1f, s*.17f)
        lineTo(s*.1f, s*.83f); cubicTo(s*.1f, s*.88f, s*.15f, s*.93f, s*.2f, s*.93f)
        lineTo(s*.8f, s*.93f); cubicTo(s*.85f, s*.93f, s*.9f, s*.88f, s*.9f, s*.83f)
        lineTo(s*.9f, s*.37f); close()
        moveTo(s*.6f, s*.07f); lineTo(s*.6f, s*.37f); lineTo(s*.9f, s*.37f)
    }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    val tick = androidx.compose.ui.graphics.Path().apply { moveTo(s*.32f, s*.62f); lineTo(s*.45f, s*.72f); lineTo(s*.68f, s*.52f) }
    drawPath(tick, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun HomeIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.1f, s*.5f); lineTo(s*.5f, s*.1f); lineTo(s*.9f, s*.5f)
        moveTo(s*.2f, s*.45f); lineTo(s*.2f, s*.88f); lineTo(s*.45f, s*.88f); lineTo(s*.45f, s*.65f)
        lineTo(s*.55f, s*.65f); lineTo(s*.55f, s*.88f); lineTo(s*.8f, s*.88f); lineTo(s*.8f, s*.45f)
    }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun InfoIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.42f, style = Stroke(s*.07f))
    drawLine(tint, Offset(s*.5f, s*.47f), Offset(s*.5f, s*.68f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawCircle(tint, s*.04f, center = Offset(s*.5f, s*.35f))
}
@Composable fun CheckSquareIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.1f), size = androidx.compose.ui.geometry.Size(s*.8f, s*.8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.3f, s*.5f); lineTo(s*.45f, s*.65f); lineTo(s*.7f, s*.35f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun SparklesIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.08f, center = Offset(s*.5f, s*.2f))
    drawCircle(tint, s*.06f, center = Offset(s*.75f, s*.45f))
    drawCircle(tint, s*.06f, center = Offset(s*.25f, s*.6f))
    val star = androidx.compose.ui.graphics.Path().apply { moveTo(s*.5f, s*.4f); lineTo(s*.55f, s*.55f); lineTo(s*.7f, s*.55f); lineTo(s*.58f, s*.65f); lineTo(s*.63f, s*.8f); lineTo(s*.5f, s*.72f); lineTo(s*.37f, s*.8f); lineTo(s*.42f, s*.65f); lineTo(s*.3f, s*.55f); lineTo(s*.45f, s*.55f); close() }
    drawPath(star, tint)
}
@Composable fun ScanFaceIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.15f, s*.3f); lineTo(s*.15f, s*.15f); lineTo(s*.3f, s*.15f)
        moveTo(s*.7f, s*.15f); lineTo(s*.85f, s*.15f); lineTo(s*.85f, s*.3f)
        moveTo(s*.85f, s*.7f); lineTo(s*.85f, s*.85f); lineTo(s*.7f, s*.85f)
        moveTo(s*.3f, s*.85f); lineTo(s*.15f, s*.85f); lineTo(s*.15f, s*.7f)
    }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    drawCircle(tint, s*.14f, center = Offset(s*.5f, s*.42f), style = Stroke(s*.06f))
    drawArc(tint, 0f, 180f, false, topLeft = Offset(s*.32f, s*.56f), size = androidx.compose.ui.geometry.Size(s*.36f, s*.24f), style = Stroke(s*.06f))
}
@Composable fun Volume2Icon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.25f, s*.37f); lineTo(s*.45f, s*.2f); lineTo(s*.45f, s*.8f); lineTo(s*.25f, s*.63f); lineTo(s*.1f, s*.63f); lineTo(s*.1f, s*.37f); close() }
    drawPath(p, tint, style = Stroke(s*.07f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawArc(tint, -30f, 60f, false, topLeft = Offset(s*.5f, s*.3f), size = androidx.compose.ui.geometry.Size(s*.2f, s*.4f), style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    drawArc(tint, -40f, 80f, false, topLeft = Offset(s*.57f, s*.2f), size = androidx.compose.ui.geometry.Size(s*.28f, s*.6f), style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun ServerIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.12f), size = androidx.compose.ui.geometry.Size(s*.8f, s*.3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.06f), style = Stroke(s*.07f))
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.55f), size = androidx.compose.ui.geometry.Size(s*.8f, s*.3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.06f), style = Stroke(s*.07f))
    drawCircle(tint, s*.04f, center = Offset(s*.75f, s*.27f))
    drawCircle(tint, s*.04f, center = Offset(s*.75f, s*.7f))
}
@Composable fun TagIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.5f, s*.08f); lineTo(s*.92f, s*.08f); lineTo(s*.92f, s*.5f); lineTo(s*.5f, s*.92f)
        lineTo(s*.08f, s*.92f); lineTo(s*.08f, s*.5f); close()
    }
    drawPath(p, tint, style = Stroke(s*.07f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawCircle(tint, s*.05f, center = Offset(s*.72f, s*.28f))
}
@Composable fun LanguagesIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawLine(tint, Offset(s*.1f, s*.3f), Offset(s*.6f, s*.3f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.35f, s*.15f), Offset(s*.35f, s*.3f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.15f, s*.3f); cubicTo(s*.15f, s*.3f, s*.25f, s*.65f, s*.35f, s*.65f); cubicTo(s*.45f, s*.65f, s*.55f, s*.3f, s*.55f, s*.3f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    val p2 = androidx.compose.ui.graphics.Path().apply { moveTo(s*.55f, s*.88f); lineTo(s*.7f, s*.52f); lineTo(s*.85f, s*.88f); moveTo(s*.6f, s*.75f); lineTo(s*.8f, s*.75f) }
    drawPath(p2, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun PaletteIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.42f, style = Stroke(s*.07f))
    listOf(Offset(s*.35f, s*.25f), Offset(s*.6f, s*.22f), Offset(s*.75f, s*.42f), Offset(s*.65f, s*.65f), Offset(s*.38f, s*.72f)).forEach { drawCircle(tint, s*.05f, center = it) }
}
@Composable fun SunIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.18f, center = Offset(s*.5f, s*.5f), style = Stroke(s*.07f))
    (0 until 8).forEach { i ->
        val a = Math.toRadians(i * 45.0).toFloat()
        drawLine(tint, Offset(s*.5f + s*.27f * kotlin.math.cos(a), s*.5f + s*.27f * kotlin.math.sin(a)), Offset(s*.5f + s*.4f * kotlin.math.cos(a), s*.5f + s*.4f * kotlin.math.sin(a)), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}
@Composable fun MoonIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.6f, s*.1f); cubicTo(s*.35f, s*.1f, s*.1f, s*.3f, s*.1f, s*.55f); cubicTo(s*.1f, s*.8f, s*.3f, s*.95f, s*.55f, s*.95f); cubicTo(s*.75f, s*.95f, s*.92f, s*.83f, s*.92f, s*.7f); cubicTo(s*.7f, s*.75f, s*.5f, s*.6f, s*.5f, s*.4f); cubicTo(s*.5f, s*.25f, s*.55f, s*.12f, s*.6f, s*.1f) }
    drawPath(p, tint, style = Stroke(s*.07f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun SunMoonIcon(tint: Color, size: Dp) = SunIcon(tint, size)
@Composable fun ScanIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.15f, s*.3f); lineTo(s*.15f, s*.15f); lineTo(s*.3f, s*.15f)
        moveTo(s*.7f, s*.15f); lineTo(s*.85f, s*.15f); lineTo(s*.85f, s*.3f)
        moveTo(s*.85f, s*.7f); lineTo(s*.85f, s*.85f); lineTo(s*.7f, s*.85f)
        moveTo(s*.3f, s*.85f); lineTo(s*.15f, s*.85f); lineTo(s*.15f, s*.7f)
    }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    drawLine(tint, Offset(s*.2f, s*.5f), Offset(s*.8f, s*.5f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun HeartPulseIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.05f, s*.5f); lineTo(s*.25f, s*.5f); lineTo(s*.35f, s*.2f); lineTo(s*.45f, s*.8f); lineTo(s*.55f, s*.35f); lineTo(s*.65f, s*.5f); lineTo(s*.95f, s*.5f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun KeyIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.22f, center = Offset(s*.35f, s*.4f), style = Stroke(s*.07f))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.52f, s*.55f); lineTo(s*.88f, s*.9f); moveTo(s*.78f, s*.8f); lineTo(s*.78f, s*.9f); lineTo(s*.88f, s*.9f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun ChevronDownIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.25f, s*.38f); lineTo(s*.5f, s*.62f); lineTo(s*.75f, s*.38f) }
    drawPath(p, tint, style = Stroke(s*.08f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun HashIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawLine(tint, Offset(s*.15f, s*.35f), Offset(s*.85f, s*.35f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.15f, s*.65f), Offset(s*.85f, s*.65f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.35f, s*.15f), Offset(s*.25f, s*.85f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.65f, s*.15f), Offset(s*.55f, s*.85f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun CopyIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.2f, s*.2f), size = androidx.compose.ui.geometry.Size(s*.6f, s*.6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.1f), size = androidx.compose.ui.geometry.Size(s*.6f, s*.6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
}
@Composable fun DownloadIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.5f, s*.15f); lineTo(s*.5f, s*.7f); moveTo(s*.3f, s*.52f); lineTo(s*.5f, s*.7f); lineTo(s*.7f, s*.52f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawLine(tint, Offset(s*.15f, s*.82f), Offset(s*.85f, s*.82f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun XIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.25f, s*.25f); lineTo(s*.75f, s*.75f); moveTo(s*.75f, s*.25f); lineTo(s*.25f, s*.75f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun AlertCircleIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.42f, style = Stroke(s*.07f))
    drawLine(tint, Offset(s*.5f, s*.3f), Offset(s*.5f, s*.55f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawCircle(tint, s*.04f, center = Offset(s*.5f, s*.68f))
}
@Composable fun FileTextIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.6f, s*.07f); lineTo(s*.2f, s*.07f); cubicTo(s*.15f, s*.07f, s*.1f, s*.12f, s*.1f, s*.17f)
        lineTo(s*.1f, s*.83f); cubicTo(s*.1f, s*.88f, s*.15f, s*.93f, s*.2f, s*.93f)
        lineTo(s*.8f, s*.93f); cubicTo(s*.85f, s*.93f, s*.9f, s*.88f, s*.9f, s*.83f)
        lineTo(s*.9f, s*.37f); close()
        moveTo(s*.6f, s*.07f); lineTo(s*.6f, s*.37f); lineTo(s*.9f, s*.37f)
    }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawLine(tint, Offset(s*.28f, s*.55f), Offset(s*.72f, s*.55f), s*.06f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.28f, s*.68f), Offset(s*.72f, s*.68f), s*.06f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun ScanLineIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawLine(tint, Offset(s*.1f, s*.5f), Offset(s*.9f, s*.5f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.2f, s*.3f), Offset(s*.2f, s*.7f), s*.05f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.8f, s*.3f), Offset(s*.8f, s*.7f), s*.05f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun UserCheckIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.16f, center = Offset(s*.38f, s*.3f), style = Stroke(s*.07f))
    drawArc(tint, 180f, 180f, false, topLeft = Offset(s*.1f, s*.52f), size = androidx.compose.ui.geometry.Size(s*.56f, s*.35f), style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.62f, s*.58f); lineTo(s*.72f, s*.68f); lineTo(s*.88f, s*.48f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
}
@Composable fun UserXIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.16f, center = Offset(s*.38f, s*.3f), style = Stroke(s*.07f))
    drawArc(tint, 180f, 180f, false, topLeft = Offset(s*.1f, s*.52f), size = androidx.compose.ui.geometry.Size(s*.56f, s*.35f), style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.62f, s*.48f); lineTo(s*.88f, s*.68f); moveTo(s*.88f, s*.48f); lineTo(s*.62f, s*.68f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun BarChart3Icon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawLine(tint, Offset(s*.2f, s*.7f), Offset(s*.2f, s*.4f), s*.12f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.5f, s*.7f), Offset(s*.5f, s*.2f), s*.12f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.8f, s*.7f), Offset(s*.8f, s*.5f), s*.12f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.1f, s*.7f), Offset(s*.9f, s*.7f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun ClockIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.42f, style = Stroke(s*.07f))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.5f, s*.3f); lineTo(s*.5f, s*.5f); lineTo(s*.65f, s*.65f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun ImageOffIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.1f), size = androidx.compose.ui.geometry.Size(s*.8f, s*.8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.1f, s*.9f); lineTo(s*.9f, s*.1f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
}
@Composable fun FingerprintIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawArc(tint, 200f, 140f, false, topLeft = Offset(s*.2f, s*.2f), size = androidx.compose.ui.geometry.Size(s*.6f, s*.6f), style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    drawArc(tint, 210f, 120f, false, topLeft = Offset(s*.3f, s*.3f), size = androidx.compose.ui.geometry.Size(s*.4f, s*.4f), style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    drawLine(tint, Offset(s*.5f, s*.5f), Offset(s*.5f, s*.75f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun CalendarIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.2f), size = androidx.compose.ui.geometry.Size(s*.8f, s*.7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
    drawLine(tint, Offset(s*.1f, s*.4f), Offset(s*.9f, s*.4f), s*.07f)
    drawLine(tint, Offset(s*.35f, s*.12f), Offset(s*.35f, s*.28f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(s*.65f, s*.12f), Offset(s*.65f, s*.28f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
}
@Composable fun MapPinIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply {
        moveTo(s*.5f, s*.9f); cubicTo(s*.5f, s*.9f, s*.15f, s*.6f, s*.15f, s*.42f); cubicTo(s*.15f, s*.25f, s*.32f, s*.1f, s*.5f, s*.1f); cubicTo(s*.68f, s*.1f, s*.85f, s*.25f, s*.85f, s*.42f); cubicTo(s*.85f, s*.6f, s*.5f, s*.9f, s*.5f, s*.9f)
    }
    drawPath(p, tint, style = Stroke(s*.07f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawCircle(tint, s*.1f, center = Offset(s*.5f, s*.4f), style = Stroke(s*.07f))
}
@Composable fun BriefcaseIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.1f, s*.3f), size = androidx.compose.ui.geometry.Size(s*.8f, s*.55f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.07f), style = Stroke(s*.07f))
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.35f, s*.3f); lineTo(s*.35f, s*.18f); cubicTo(s*.35f, s*.13f, s*.4f, s*.1f, s*.45f, s*.1f); lineTo(s*.55f, s*.1f); cubicTo(s*.6f, s*.1f, s*.65f, s*.13f, s*.65f, s*.18f); lineTo(s*.65f, s*.3f) }
    drawPath(p, tint, style = Stroke(s*.07f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    drawLine(tint, Offset(s*.1f, s*.57f), Offset(s*.9f, s*.57f), s*.07f)
}
@Composable fun GlobeIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawCircle(tint, s*.42f, style = Stroke(s*.07f))
    drawArc(tint, -90f, 360f, false, topLeft = Offset(s*.3f, s*.08f), size = androidx.compose.ui.geometry.Size(s*.4f, s*.84f), style = Stroke(s*.07f))
    drawLine(tint, Offset(s*.08f, s*.5f), Offset(s*.92f, s*.5f), s*.07f)
}
@Composable fun RulerIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    val p = androidx.compose.ui.graphics.Path().apply { moveTo(s*.15f, s*.85f); lineTo(s*.85f, s*.15f) }
    drawPath(p, tint, style = Stroke(s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    listOf(0.25f, 0.38f, 0.5f, 0.62f, 0.75f).forEachIndexed { i, t2 ->
        val len = if (i == 2) 0.12f else 0.07f
        val angle = Math.toRadians(45.0).toFloat()
        val cx = s * t2; val cy = s * (1 - t2)
        drawLine(tint, Offset(cx - len * s * kotlin.math.cos(angle + 1.57f), cy - len * s * kotlin.math.sin(angle + 1.57f)), Offset(cx + len * s * kotlin.math.cos(angle + 1.57f), cy + len * s * kotlin.math.sin(angle + 1.57f)), s*.05f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    }
}
@Composable fun BuildingIcon(tint: Color, size: Dp) = IconCanvas(tint, size) { s ->
    drawRoundRect(tint, topLeft = Offset(s*.2f, s*.1f), size = androidx.compose.ui.geometry.Size(s*.6f, s*.8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s*.04f), style = Stroke(s*.07f))
    drawLine(tint, Offset(s*.1f, s*.9f), Offset(s*.9f, s*.9f), s*.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
    listOf(Offset(s*.35f, s*.28f), Offset(s*.55f, s*.28f), Offset(s*.35f, s*.48f), Offset(s*.55f, s*.48f)).forEach { drawRect(tint, topLeft = it, size = androidx.compose.ui.geometry.Size(s*.1f, s*.12f)) }
    drawRect(tint, topLeft = Offset(s*.41f, s*.65f), size = androidx.compose.ui.geometry.Size(s*.18f, s*.25f))
}

// ─── Generic Canvas icon helper ───────────────────────────────────────────────

@Composable
fun IconCanvas(tint: Color, size: Dp, draw: androidx.compose.ui.graphics.drawscope.DrawScope.(Float) -> Unit) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
        draw(this.size.width)
    }
}
