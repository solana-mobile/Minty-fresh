package com.solanamobile.mintyfresh.mintycore.injection

import com.metaplex.lib.drivers.rpc.JdkRpcDriver
import com.metaplex.lib.drivers.solana.Commitment
import com.metaplex.lib.drivers.solana.Connection
import com.metaplex.lib.drivers.solana.SolanaConnectionDriver
import com.metaplex.lib.drivers.solana.TransactionOptions
import com.solanamobile.mintyfresh.mintycore.BuildConfig
import com.solanamobile.mintyfresh.mintycore.endpoints.*
import com.solanamobile.mintyfresh.networkinterface.rpcconfig.IRpcConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

@Module
@InstallIn(
    ViewModelComponent::class
)
class MintyCoreModule {

    @Provides
    fun providesNftStorageApi(okHttpClient: OkHttpClient): NftStorageEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(NftStorageResponseConverter)
            .build()

        return retrofit.create(NftStorageEndpoints::class.java)
    }

    @Provides
    fun providesArweaveApi(okHttpClient: OkHttpClient): ArweaveEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.ARWEAVE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(object : Converter.Factory() {
                override fun responseBodyConverter(
                    type: Type,
                    annotations: Array<out Annotation>,
                    retrofit: Retrofit
                ): Converter<ResponseBody, *>? {
                    return when(type) {
                        java.lang.String::class.java -> Converter {
                            it.string()
                        }
                        else -> super.responseBodyConverter(type, annotations, retrofit)
                    }
                }
            })
            .build()

        return retrofit.create(ArweaveEndpoints::class.java)
    }

    @Provides
    fun providesBundlrApi(okHttpClient: OkHttpClient): BundlerEndpoints {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BUNDLER_NODE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(BundlerApiConverter)
            .build()

        return retrofit.create(BundlerEndpoints::class.java)
    }

    @Provides
    fun providesMetaplexConnectionDriver(rpcConfig: IRpcConfig): Connection =
        SolanaConnectionDriver(
            JdkRpcDriver(rpcConfig.solanaRpcUrl),
            TransactionOptions(Commitment.CONFIRMED, skipPreflight = true)
        )

    @Provides
    fun providesOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor {
                val request = it.request()
                println("request:")
                println("   url: ${request.url}")
                println("   body: ${request.body}")
                val response = try {
                    it.proceed(request)
                } catch (e: Exception) {
                    println("wtffffffff: ${e.stackTraceToString()}")
                    throw Error("EGATS!!")
                }
                println("response:")
                println("   code: ${response.code}")
                println("   message: ${response.message}")
                println("   body: ${response.peekBody(4000).string()}")
                response
            }
            .build()
    }
}