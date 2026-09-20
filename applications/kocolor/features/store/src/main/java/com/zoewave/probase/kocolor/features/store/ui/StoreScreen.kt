package com.zoewave.probase.kocolor.features.store.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.zoewave.probase.kocolor.features.store.R
import com.zoewave.probase.kocolor.model.KoColorRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    uiState: StoreUiState,
    modifier: Modifier = Modifier,
    onEvent: (StoreEvent) -> Unit,
    navTo: (KoColorRoute) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.applications_kocolor_features_store_title),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1A1A1A)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navTo(KoColorRoute.Back) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.applications_kocolor_features_store_back), tint = Color.Black)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { onEvent(StoreEvent.OpenCart) }
                    ) {
                        IconButton(onClick = { onEvent(StoreEvent.OpenCart) }) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = "Bag", tint = Color.Black)
                        }
                        if (uiState.cartCount > 0) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF6B3A8B),
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${uiState.cartCount}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFFBF8F9)
    ) { padding ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            
            // 1. Atelier Boutique Hero Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFFCF3F5), Color(0xFFF7E6EC))
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // DNA Pill Badge
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFE8D3D8).copy(alpha = 0.7f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF8B5A52)))
                                    Text(
                                        text = "CHROMATIC DNA: MUTED AUTUMN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6B3A32),
                                        fontSize = 9.sp,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = stringResource(R.string.applications_kocolor_features_store_boutique_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    letterSpacing = 2.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.applications_kocolor_features_store_boutique_title),
                                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A)
                                )
                            }

                            Text(
                                text = "Enter our curated boutique where science meets aesthetics. Tailored formulas and archival silhouettes matched to your chromatic harmony.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = Color(0xFF4A4A4A)
                            )
                        }
                    }
                }
            }

            // 2. Three-Column Trust / Feature Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFF0ECEF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrustFeatureItem("SPECTRAL MATCH", "99.4% Accuracy")
                        VerticalDivider(modifier = Modifier.height(28.dp), color = Color(0xFFE8E2E5))
                        TrustFeatureItem("CLEAN BIOMETRICS", "Vegan & Pure")
                        VerticalDivider(modifier = Modifier.height(28.dp), color = Color(0xFFE8E2E5))
                        TrustFeatureItem("ATELIER COURIER", "Complimentary")
                    }
                }
            }

            // 3. Cosmetics Vault Section
            item {
                val cosmeticItems = remember(uiState.realCosmeticItems, uiState.cosmeticsPacks) {
                    if (uiState.realCosmeticItems.isNotEmpty()) {
                        uiState.realCosmeticItems
                    } else if (uiState.cosmeticsPacks.isNotEmpty()) {
                        uiState.cosmeticsPacks.map { pack ->
                            StoreProductItem(
                                id = pack.id,
                                title = pack.name,
                                subtitle = pack.description,
                                category = "COSMETICS",
                                price = "$${(28..78).random()}",
                                detailTag = "${pack.itemCount} items",
                                badge = "98% Harmony",
                                shadeName = pack.previewItems.firstOrNull()?.name?.uppercase() ?: "SIGNATURE FORMULA",
                                shadeColor = Color(0xFFD4AF37),
                                imageModel = pack.heroImageUrl ?: R.drawable.applications_kocolor_features_store_kocolor_fabric_clean
                            )
                        }
                    } else emptyList()
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_store_cosmetics_vault),
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFE91E63)))
                        }
                        Surface(
                            onClick = { navTo(KoColorRoute.StarterPack(filter = "cosmetics")) },
                            shape = RoundedCornerShape(50),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFECE4EE)),
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val totalCosmeticsCount = if (uiState.cosmeticsPacks.isNotEmpty()) uiState.cosmeticsPacks.sumOf { it.itemCount } else 173
                                Text(
                                    text = stringResource(R.string.applications_kocolor_features_store_view_all_format, totalCosmeticsCount),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C1A2E),
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF2C1A2E),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(cosmeticItems) { item ->
                            ProductCard(
                                item = item,
                                onClick = { navTo(KoColorRoute.PackPreview(packId = item.id)) },
                                onAddClick = { onEvent(StoreEvent.AddProductToInventory(item.id)) },
                                onFavoriteClick = { onEvent(StoreEvent.ToggleFavorite(item.id)) }
                            )
                        }
                    }
                }
            }

            // 4. Fashion Archive Section
            item {
                val fashionItems = remember(uiState.realFashionItems, uiState.fashionPacks) {
                    if (uiState.realFashionItems.isNotEmpty()) {
                        uiState.realFashionItems
                    } else if (uiState.fashionPacks.isNotEmpty()) {
                        uiState.fashionPacks.map { pack ->
                            StoreProductItem(
                                id = pack.id,
                                title = pack.name,
                                subtitle = pack.description,
                                category = "OUTERWEAR",
                                price = "$${(120..420).random()}",
                                detailTag = "${pack.itemCount} items",
                                badge = "Warm Tone",
                                imageModel = pack.heroImageUrl ?: R.drawable.applications_kocolor_features_store_kocolor_fabric_clean
                            )
                        }
                    } else emptyList()
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.applications_kocolor_features_store_fashion_archive),
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD4AF37)))
                        }
                        Surface(
                            onClick = { navTo(KoColorRoute.StarterPack(filter = "clothing")) },
                            shape = RoundedCornerShape(50),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFECE4EE)),
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.applications_kocolor_features_store_explore_all),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2C1A2E),
                                    fontSize = 11.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF2C1A2E),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(fashionItems) { item ->
                            ProductCard(
                                item = item,
                                onClick = { navTo(KoColorRoute.PackPreview(packId = item.id)) },
                                onAddClick = { onEvent(StoreEvent.AddProductToInventory(item.id)) },
                                onFavoriteClick = { onEvent(StoreEvent.ToggleFavorite(item.id)) }
                            )
                        }
                    }
                }
            }

            // 5. Chromatic Specialist Banner (Bottom Reserve Session)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E111F) // Deep midnight plum
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "CHROMATIC SPECIALIST",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE0A8BB),
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Book Virtual Palette Session",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "1-on-1 shade calibration with our master stylist.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC0B0C4),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Surface(
                            onClick = { /* Reserve Session */ },
                            shape = RoundedCornerShape(50),
                            color = Color.White
                        ) {
                            Text(
                                text = "Reserve",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isCartOpen) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(StoreEvent.CloseCart) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Atelier Shopping Cart",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "${uiState.cartCount} items",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)

                if (uiState.cartItems.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Text("Your shopping cart is empty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Tap 'Add +' on any store item to add it to your cart.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                } else {
                    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.cartItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF9F6F8), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFEDE7EA))
                                    ) {
                                        if (item.imageModel != null) {
                                            AsyncImage(
                                                model = item.imageModel,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                    Column {
                                        Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(item.price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    IconButton(
                                        onClick = { onEvent(StoreEvent.RemoveFromCart(item.id)) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.08f), thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Value", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Text(
                            currencyFormatter.format(uiState.cartTotalPrice),
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Surface(
                        onClick = { onEvent(StoreEvent.CheckoutCart) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF1E111F)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CHECKOUT & INGEST TO VAULT", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (uiState.isCheckoutSuccess) {
        AlertDialog(
            onDismissRequest = { onEvent(StoreEvent.ClearCheckoutSuccess) },
            title = { Text("Items Added to Vault!", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            text = { Text("Your selected Atelier items have been successfully checked out and ingested into your active Vanity & Wardrobe Vaults.") },
            confirmButton = {
                TextButton(onClick = {
                    onEvent(StoreEvent.ClearCheckoutSuccess)
                    navTo(KoColorRoute.Home)
                }) {
                    Text("VIEW COLLECTION HUB", fontWeight = FontWeight.Black, color = Color(0xFF6B3A8B))
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun TrustFeatureItem(
    title: String,
    subtitle: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B5A52),
            fontSize = 8.sp,
            letterSpacing = 0.5.sp
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = Color.DarkGray,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ProductCard(
    item: StoreProductItem,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFF0ECEF)),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFFF3ECEF))
            ) {
                if (item.imageModel != null) {
                    AsyncImage(
                        model = item.imageModel,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Top-Left Badge
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = item.badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Top-Right Heart Icon
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .clickable(onClick = onFavoriteClick),
                    shape = CircleShape,
                    color = if (item.isFavorite) Color(0xFFFFEBEE) else Color.White.copy(alpha = 0.85f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Color(0xFFE91E63) else Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Shade / Category Subtitle
                if (item.shadeName != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item.shadeColor?.let { col ->
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(col))
                        }
                        Text(
                            text = item.shadeName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5A52),
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5A52),
                        fontSize = 8.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.shadeName != null) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.price,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    if (item.isAdded) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF1B5E20)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Added", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.Black,
                            modifier = Modifier.clickable(onClick = onAddClick)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text("Add", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StoreScreenPreview() {
    MaterialTheme {
        StoreScreen(
            uiState = StoreUiState(),
            onEvent = {},
            navTo = {}
        )
    }
}
