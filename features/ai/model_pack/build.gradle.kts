plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("ai_model_pack")
    dynamicDelivery {
        deliveryType.set("on-demand")
    }
}
