package com.yallakhedma.app.di

import com.yallakhedma.app.data.datasource.CategoriesFirestoreDataSource
import com.yallakhedma.app.data.datasource.CouponsFirestoreDataSource
import com.yallakhedma.app.data.datasource.FirebaseAuthDataSource
import com.yallakhedma.app.data.crypto.PaymentCardCrypto
import com.yallakhedma.app.data.datasource.PaymentMethodsFirestoreDataSource
import com.yallakhedma.app.data.datasource.ProvidersFirestoreDataSource
import com.yallakhedma.app.data.datasource.RequestsFirestoreDataSource
import com.yallakhedma.app.data.datasource.ServicesFirestoreDataSource
import com.yallakhedma.app.data.datasource.StorageImagesDataSource
import com.yallakhedma.app.data.datasource.UserFirestoreDataSource
import com.yallakhedma.app.data.datasource.VerificationStorageDataSource
import com.yallakhedma.app.data.repository.AuthRepositoryImpl
import com.yallakhedma.app.data.repository.CategoryRepositoryImpl
import com.yallakhedma.app.data.repository.CouponRepositoryImpl
import com.yallakhedma.app.data.repository.PaymentMethodRepositoryImpl
import com.yallakhedma.app.data.repository.ProviderRepositoryImpl
import com.yallakhedma.app.data.repository.RequestRepositoryImpl
import com.yallakhedma.app.data.repository.ServiceRepositoryImpl
import com.yallakhedma.app.data.repository.VerificationRepositoryImpl
import com.yallakhedma.app.domain.repository.AuthRepository
import com.yallakhedma.app.domain.repository.CategoryRepository
import com.yallakhedma.app.domain.repository.CouponRepository
import com.yallakhedma.app.domain.repository.PaymentMethodRepository
import com.yallakhedma.app.domain.repository.ProviderRepository
import com.yallakhedma.app.domain.repository.RequestRepository
import com.yallakhedma.app.domain.repository.ServiceRepository
import com.yallakhedma.app.domain.repository.VerificationRepository
import com.yallakhedma.app.presentation.screens.auth.login.LoginScreenModel
import com.yallakhedma.app.presentation.screens.auth.signup.SignUpScreenModel
import com.yallakhedma.app.presentation.screens.bookings.BookingDetailsScreenModel
import com.yallakhedma.app.presentation.screens.bookings.BookingRequestScreenModel
import com.yallakhedma.app.presentation.screens.bookings.MyBookingsScreenModel
import com.yallakhedma.app.presentation.screens.bookings.PayBookingScreenModel
import com.yallakhedma.app.presentation.screens.bookings.ProviderBookingsScreenModel
import com.yallakhedma.app.presentation.screens.categories.AllCategoriesScreenModel
import com.yallakhedma.app.presentation.screens.coupons.AddCouponScreenModel
import com.yallakhedma.app.presentation.screens.coupons.ProviderCouponsScreenModel
import com.yallakhedma.app.presentation.screens.dashboard.ProviderDashboardScreenModel
import com.yallakhedma.app.presentation.screens.home.ClientHomeScreenModel
import com.yallakhedma.app.presentation.screens.profile.ClientEditProfileScreenModel
import com.yallakhedma.app.presentation.screens.profile.ClientProfileScreenModel
import com.yallakhedma.app.presentation.screens.payment.AddPaymentMethodScreenModel
import com.yallakhedma.app.presentation.screens.payment.PaymentMethodsScreenModel
import com.yallakhedma.app.presentation.screens.profile.ProviderEditProfileScreenModel
import com.yallakhedma.app.presentation.screens.profile.ProviderProfileScreenModel
import com.yallakhedma.app.presentation.screens.provider_setup.ProviderProfileSetupScreenModel
import com.yallakhedma.app.presentation.screens.providers.ProviderDetailsScreenModel
import com.yallakhedma.app.presentation.screens.services.AddServiceScreenModel
import com.yallakhedma.app.presentation.screens.services.MyServicesScreenModel
import com.yallakhedma.app.presentation.screens.services.ServiceDetailsScreenModel
import com.yallakhedma.app.presentation.screens.verification.IdVerificationScreenModel
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    // Data sources
    single { FirebaseAuthDataSource() }
    single { UserFirestoreDataSource() }
    single { io.ktor.client.HttpClient() }
    single { com.yallakhedma.app.data.remote.OpeninaryClient(get()) }
    single { VerificationStorageDataSource(get()) }
    single { StorageImagesDataSource(get()) }
    single { ProvidersFirestoreDataSource() }
    single { ServicesFirestoreDataSource() }
    single { CategoriesFirestoreDataSource() }
    single { CouponsFirestoreDataSource() }
    single { RequestsFirestoreDataSource() }
    single { PaymentMethodsFirestoreDataSource() }
    single { PaymentCardCrypto() }
    single { com.yallakhedma.app.data.datasource.ChatsFirestoreDataSource() }
    single { com.yallakhedma.app.data.datasource.SubscriptionsFirestoreDataSource() }
    single { com.yallakhedma.app.data.datasource.OpenRequestsFirestoreDataSource() }
    single { com.yallakhedma.app.util.ClientLocationHolder() }
    single { com.yallakhedma.app.data.local.AppPreferences(get()) }
    single { com.yallakhedma.app.presentation.i18n.LanguageManager(get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<VerificationRepository> { VerificationRepositoryImpl(get(), get()) }
    single<ProviderRepository> { ProviderRepositoryImpl(get()) }
    single<ServiceRepository> { ServiceRepositoryImpl(get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(get()) }
    single<CouponRepository> { CouponRepositoryImpl(get()) }
    single<RequestRepository> { RequestRepositoryImpl(get()) }
    single<PaymentMethodRepository> { PaymentMethodRepositoryImpl(get()) }
    single<com.yallakhedma.app.domain.repository.ChatRepository> { com.yallakhedma.app.data.repository.ChatRepositoryImpl(get()) }
    single<com.yallakhedma.app.domain.repository.SubscriptionRepository> { com.yallakhedma.app.data.repository.SubscriptionRepositoryImpl(get()) }
    single<com.yallakhedma.app.domain.repository.OpenRequestRepository> { com.yallakhedma.app.data.repository.OpenRequestRepositoryImpl(get()) }

    // ScreenModels (factory: new instance per screen)
    factory { LoginScreenModel(get()) }
    factory { SignUpScreenModel(get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.auth.verify.EmailVerificationScreenModel(get(), get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.auth.welcome.WelcomeScreenModel(get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.auth.forgot.ForgotPasswordScreenModel(get()) }
    factory { com.yallakhedma.app.presentation.screens.auth.usertype.UserTypeScreenModel(get()) }
    factory { IdVerificationScreenModel(get(), get()) }
    factory { ClientHomeScreenModel(get(), get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.explore.ExploreScreenModel(get(), get(), get(), get()) }
    factory { AllCategoriesScreenModel(get()) }
    factory { ServiceDetailsScreenModel(get(), get(), get()) }
    factory { ProviderDetailsScreenModel(get(), get()) }
    factory { ProviderProfileSetupScreenModel(get(), get(), get()) }
    factory { ProviderDashboardScreenModel(get(), get(), get()) }
    factory { AddServiceScreenModel(get(), get(), get(), get()) }
    factory { MyServicesScreenModel(get(), get()) }
    factory { ClientProfileScreenModel(get()) }
    factory { ProviderProfileScreenModel(get(), get()) }
    factory { ClientEditProfileScreenModel(get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.profile.ConfirmIdentityScreenModel(get(), get()) }
    factory { ProviderEditProfileScreenModel(get(), get(), get()) }
    factory { PaymentMethodsScreenModel(get(), get()) }
    factory { AddPaymentMethodScreenModel(get(), get(), get()) }
    factory { MyBookingsScreenModel(get(), get(), get()) }
    factory { BookingDetailsScreenModel(get(), get()) }
    factory { BookingRequestScreenModel(get(), get(), get()) }
    factory { PayBookingScreenModel(get(), get(), get(), get()) }
    factory { ProviderBookingsScreenModel(get(), get()) }
    factory { ProviderCouponsScreenModel(get(), get()) }
    factory { AddCouponScreenModel(get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.chats.ChatsScreenModel(get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.chats.ChatScreenModel(get(), get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.subscription.SubscriptionCheckoutScreenModel(get(), get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.requests.NewRequestScreenModel(get(), get(), get(), get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.requests.MyRequestsScreenModel(get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.requests.OpenRequestDetailsScreenModel(get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.requests.OpenJobsScreenModel(get(), get(), get()) }
    factory { com.yallakhedma.app.presentation.screens.requests.OpenJobDetailsScreenModel(get(), get(), get()) }
}

expect val platformModule: Module
