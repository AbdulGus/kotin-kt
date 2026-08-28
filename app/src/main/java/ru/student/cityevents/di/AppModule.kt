package ru.student.cityevents.di

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.student.cityevents.data.EventsRepository
import ru.student.cityevents.data.OfflineFirstEventsRepository
import ru.student.cityevents.data.local.AppDatabase
import ru.student.cityevents.data.local.EventDao
import ru.student.cityevents.data.remote.KudaGoApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "city_events.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

    @Provides fun provideDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides @Singleton
    fun provideApi(): KudaGoApi {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return Retrofit.Builder()
            .baseUrl("https://kudago.com/")
            .client(OkHttpClient.Builder().addInterceptor(logger).build())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(KudaGoApi::class.java)
    }

    @Provides @Singleton
    fun provideRepository(repository: OfflineFirstEventsRepository): EventsRepository = repository
}
