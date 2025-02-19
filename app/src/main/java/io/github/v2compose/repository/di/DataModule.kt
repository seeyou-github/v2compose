package io.github.v2compose.repository.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.repository.AppRepository
import io.github.v2compose.repository.NewsRepository
import io.github.v2compose.repository.NodeRepository
import io.github.v2compose.repository.TopicRepository
import io.github.v2compose.repository.UserRepository
import io.github.v2compose.repository.def.DefaultAccountRepository
import io.github.v2compose.repository.def.DefaultAppRepository
import io.github.v2compose.repository.def.DefaultNewsRepository
import io.github.v2compose.repository.def.DefaultNodeRepository
import io.github.v2compose.repository.def.DefaultTopicRepository
import io.github.v2compose.repository.def.DefaultUserRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun provideAppRepository(defaultAppRepository: DefaultAppRepository): AppRepository

    @Binds
    abstract fun provideNewsRepository(defaultNewsRepository: DefaultNewsRepository): NewsRepository

    @Binds
    abstract fun provideNodeRepository(defaultNodeRepository: DefaultNodeRepository): NodeRepository

    @Binds
    abstract fun provideTopicRepository(defaultTopicRepository: DefaultTopicRepository): TopicRepository

    @Binds
    abstract fun provideUserRepository(defaultUserRepository: DefaultUserRepository): UserRepository

    @Binds
    abstract fun provideAccountRepository(defaultAccountRepository: DefaultAccountRepository): AccountRepository

}