package com.yallakhedma.app.presentation.screens.auth.usertype

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.yallakhedma.app.domain.model.UserType
import com.yallakhedma.app.presentation.components.AppPrimaryButton
import com.yallakhedma.app.presentation.screens.auth.signup.SignUpScreen

private val Background = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF2F3FF)
private val OnSurface = Color(0xFF131B2E)
private val OnSurfaceVariant = Color(0xFF5B4137)
private val OutlineVariant = Color(0xFFE4BEB1)
private val LogoOrange = Color(0xFFFF6B35)
private val Primary = Color(0xFFFF6B35)
private val PrimaryFixed = Color(0xFFFFDBCE)

object UserTypeSelectionScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var selected by remember { mutableStateOf<UserType?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 24.dp)
                .padding(top = 46.dp, bottom = 24.dp),
        ) {
            // Back
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { navigator.pop() },
                contentAlignment = Alignment.Center,
            ) {
                Text("→", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "كيف تحب تستخدم يلّا خِدمة؟",
                color = OnSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "اختر نوع حسابك عشان نجهز لك التجربة المناسبة",
                color = OnSurfaceVariant,
                fontSize = 14.sp,
            )

            Spacer(Modifier.height(32.dp))
            TypeCard(
                emoji = "🔍",
                title = "أبحث عن خدمة",
                subtitle = "اعثر على أفضل مقدمي الخدمات",
                selected = selected == UserType.Client,
                onClick = { selected = UserType.Client },
            )
            Spacer(Modifier.height(16.dp))
            TypeCard(
                emoji = "🧰",
                title = "أقدّم خدمة",
                subtitle = "اعرض خدماتك ووسّع أعمالك",
                selected = selected == UserType.Provider,
                onClick = { selected = UserType.Provider },
            )

            Spacer(Modifier.weight(1f))
            AppPrimaryButton(
                text = "يلّا نبدأ",
                onClick = { selected?.let { navigator.push(SignUpScreen(it)) } },
                enabled = selected != null,
            )
        }
    }
}

@Composable
private fun TypeCard(
    emoji: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) PrimaryFixed.copy(alpha = 0.35f) else SurfaceContainerLow)
            .border(
                BorderStroke(if (selected) 2.dp else 1.dp, if (selected) LogoOrange else OutlineVariant.copy(alpha = 0.5f)),
                RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 26.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = OnSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = OnSurfaceVariant, fontSize = 13.sp)
        }
        // Selection indicator
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) LogoOrange else Color.Transparent)
                .border(
                    BorderStroke(2.dp, if (selected) LogoOrange else OutlineVariant),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
