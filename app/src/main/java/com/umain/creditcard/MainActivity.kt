package com.umain.creditcard

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umain.creditcard.ui.theme.CreditCardDemoTheme

// --- Constants ---

// Card type constants
private const val CARD_TYPE_VISA = "visa"
private const val CARD_TYPE_AMEX = "amex"
private const val CARD_TYPE_MASTERCARD = "mastercard"
private const val CARD_TYPE_DISCOVER = "discover"
private const val CARD_TYPE_UNKNOWN = "unknown"

// Card masks
private const val AMEX_CARD_MASK = "#### ###### #####"
private const val OTHER_CARD_MASK = "#### #### #### ####"

// Image URLs from the original Vue.js demo
object CardImageUrls {
    private const val BASE_URL = "https://raw.githubusercontent.com/muhammederdem/credit-card-form/master/src/assets/images/"
    const val CHIP = "${BASE_URL}chip.png"

    fun getBackgroundUrl(id: Int) = "$BASE_URL$id.jpeg"

    fun getCardLogoUrl(cardType: String) = "$BASE_URL$cardType.png"
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CreditCardDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CreditCardForm()
                }
            }
        }
    }
}

fun getCardType(number: String): String =
    when {
        number.startsWith("4") -> CARD_TYPE_VISA
        number.startsWith("34") || number.startsWith("37") -> CARD_TYPE_AMEX
        number.matches(Regex("^5[1-5].*")) -> CARD_TYPE_MASTERCARD
        number.startsWith("6011") -> CARD_TYPE_DISCOVER
        else -> CARD_TYPE_UNKNOWN
    }

fun getCardMask(cardType: String): String = if (cardType == CARD_TYPE_AMEX) AMEX_CARD_MASK else OTHER_CARD_MASK

class CreditCardVisualTransformation(
    private val cardType: String,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val mask = getCardMask(cardType)
        val trimmed =
            if (text.text.length >= mask.replace(" ", "").length) {
                text.text.substring(0 until mask.replace(" ", "").length)
            } else {
                text.text
            }

        var out = ""
        var maskIndex = 0
        var textIndex = 0
        while (maskIndex < mask.length && textIndex < trimmed.length) {
            if (mask[maskIndex] == '#') {
                out += trimmed[textIndex]
                textIndex++
            } else {
                out += mask[maskIndex]
            }
            maskIndex++
        }

        return TransformedText(
            AnnotatedString(out),
            creditCardOffsetTranslator(mask),
        )
    }
}

private fun creditCardOffsetTranslator(mask: String) =
    object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            var noneDigitCount = 0
            var i = 0
            while (i < offset + noneDigitCount) {
                if (mask[i] != '#') {
                    noneDigitCount++
                }
                i++
            }
            return offset + noneDigitCount
        }

        override fun transformedToOriginal(offset: Int): Int = offset - mask.take(offset).count { it != '#' }
    }

// --- Main Composables ---

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCreditCardNumber(
    number: String,
    cardType: String,
) {
    val mask = getCardMask(cardType)

    Row {
        var digitIndex = 0
        mask.forEach { maskChar ->
            if (maskChar == '#') {
                val char = number.getOrNull(digitIndex) ?: '#'

                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "cardNumberCharAnimation",
                ) { targetChar ->
                    Text(
                        text = targetChar.toString(),
                        style =
                            TextStyle(
                                color = Color.White,
                                fontSize = 22.sp,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp,
                            ),
                    )
                }
                digitIndex++
            } else {
                Text(
                    text = maskChar.toString(),
                    style =
                        TextStyle(
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                        ),
                )
            }
        }
    }
}

@Composable
fun CreditCardForm() {
    val context = LocalContext.current
    var cardName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var focusedField by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val randomBackgroundId by remember { mutableIntStateOf((1..25).random()) }

    val cardType = getCardType(cardNumber)
    val isFlipped = focusedField == "cvv"

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFDDEEFC))
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        CardPreview(
            name = cardName,
            number = cardNumber,
            expiry = expiryDate,
            cvv = cardCvv,
            cardType = cardType,
            isFlipped = isFlipped,
            backgroundUrl = CardImageUrls.getBackgroundUrl(randomBackgroundId),
        )

        Spacer(Modifier.height(32.dp))

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp),
        ) {
            OutlinedTextField(
                value = cardNumber,
                onValueChange = {
                    val maxDigits = getCardMask(cardType).replace(" ", "").length
                    if (it.length <= maxDigits) {
                        cardNumber = it.filter { char -> char.isDigit() }
                    }
                },
                label = { Text("Card Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CreditCardVisualTransformation(cardType),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedField = "number" },
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = cardName,
                onValueChange = { cardName = it },
                label = { Text("Card Holder") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Characters),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (it.isFocused) focusedField = "name" },
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = {
                        if (it.length <= 4) {
                            expiryDate = it.filter { char -> char.isDigit() }
                        }
                    },
                    label = { Text("Expires (MM/YY)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ExpiryDateVisualTransformation(),
                    modifier =
                        Modifier
                            .weight(1f)
                            .onFocusChanged { if (it.isFocused) focusedField = "expiry" },
                )
                OutlinedTextField(
                    value = cardCvv,
                    onValueChange = {
                        val maxLength = if (cardType == CARD_TYPE_AMEX) 4 else 3
                        if (it.length <= maxLength) {
                            cardCvv = it.filter { char -> char.isDigit() }
                        }
                    },
                    label = { Text("CVV") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier =
                        Modifier
                            .weight(1f)
                            .onFocusChanged { if (it.isFocused) focusedField = "cvv" },
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    Toast.makeText(context, "Submitted", Toast.LENGTH_SHORT).show()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("Submit", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CardPreview(
    name: String,
    number: String,
    expiry: String,
    cvv: String,
    cardType: String,
    isFlipped: Boolean,
    backgroundUrl: String,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "flipAnimation",
    )

    Box(
        modifier =
            Modifier
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12 * density
                }.fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
    ) {
        if (rotation <= 90f) {
            CardFront(name, number, expiry, cardType, backgroundUrl)
        } else {
            CardBack(cvv, backgroundUrl, modifier = Modifier.graphicsLayer { rotationY = 180f })
        }
    }
}

@Composable
fun CardFront(
    name: String,
    number: String,
    expiry: String,
    cardType: String,
    backgroundUrl: String,
) {
    Box {
        AsyncImage(
            model = backgroundUrl,
            contentDescription = "Card Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                            startY = 300f,
                        ),
                    ),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = CardImageUrls.CHIP,
                    contentDescription = "Card Chip",
                    modifier = Modifier.size(50.dp),
                )
                if (cardType != CARD_TYPE_UNKNOWN) {
                    AsyncImage(
                        model = CardImageUrls.getCardLogoUrl(cardType),
                        contentDescription = "$cardType Logo",
                        modifier = Modifier.height(40.dp),
                    )
                }
            }

            Column {
                AnimatedCreditCardNumber(number = number, cardType = cardType)

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Card Holder", color = Color.LightGray, fontSize = 12.sp)
                        Text(
                            name.ifEmpty { "FULL NAME" }.uppercase(),
                            color = Color.White,
                            fontSize = 16.sp,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Expires", color = Color.LightGray, fontSize = 12.sp)
                        Text(
                            expiry.let { if (it.length >= 2) it.substring(0, 2) + "/" + it.substring(2) else it }.ifEmpty { "MM/YY" },
                            color = Color.White,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardBack(
    cvv: String,
    backgroundUrl: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AsyncImage(
            model = backgroundUrl,
            contentDescription = "Card Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(Color.Black.copy(alpha = 0.8f)),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(Color.White, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "*".repeat(cvv.length),
                    modifier = Modifier.padding(end = 12.dp),
                    style =
                        TextStyle(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                        ),
                )
            }
        }
    }
}

fun maskCardNumber(
    number: String,
    mask: String,
): String {
    var result = ""
    var digitIndex = 0
    mask.forEach { char ->
        result +=
            if (char == '#') {
                if (digitIndex < number.length) number[digitIndex++] else '#'
            } else {
                char
            }
    }
    return result
}

class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += "/"
        }

        val offsetTranslator =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 1) return offset
                    return offset + 1
                }

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset <= 2) return offset
                    return offset - 1
                }
            }

        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun CreditCardFormPreview() {
    CreditCardDemoTheme {
        CreditCardForm()
    }
}
