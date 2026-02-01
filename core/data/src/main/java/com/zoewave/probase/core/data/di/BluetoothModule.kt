package com.zoewave.probase.core.data.di

import com.zoewave.probase.core.data.repository.bluetoothLE.BluetoothJuul
import com.zoewave.probase.core.data.repository.bluetoothLE.BluetoothJuulImpl
import com.zoewave.probase.core.data.repository.bluetoothLE.BluetoothLeRepImpl
import com.zoewave.probase.core.data.repository.bluetoothLE.BluetoothLeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class BluetoothModule {

    @Binds
    @Singleton
    abstract fun bindBluetoothLeRepository(
        impl: BluetoothLeRepImpl
    ): BluetoothLeRepository


    @Binds
    abstract fun bindBluetoothJuulRepository(
        impl: BluetoothJuulImpl
    ): BluetoothJuul

}
