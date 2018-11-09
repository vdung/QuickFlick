package vdung.android.quickflick.data

import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.UnicastProcessor
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

abstract class ResultPublisher<LocalType, NetworkArg, NetworkType> : Publisher<Result<LocalType>> {

    protected abstract fun localData(): Publisher<LocalType>
    protected abstract fun shouldFetch(arg: NetworkArg, previousResult: LocalType): Boolean
    protected abstract fun fetchFromNetwork(arg: NetworkArg): Publisher<NetworkType>

    private val fetchProcessor = UnicastProcessor.create<NetworkArg>()

    private val flow: Publisher<Result<LocalType>> by lazy {
        val fetchArgs = fetchProcessor.onBackpressureLatest().publish().autoConnect()
        return@lazy Flowable.fromPublisher(localData())
            .switchMap { result ->
                fetchArgs
                    .filter { shouldFetch(it, result) }
                    .switchMap { arg ->
                        Single.fromPublisher(fetchFromNetwork(arg))
                            .doOnSuccess(this::onNetworkResult)
                            .ignoreElement()
                            .andThen(Flowable.empty<Result<LocalType>>())
                            .startWith(Result.Pending(result))
                            .onErrorReturn { Result.Error(it, result) }
                    }
                    .startWith(Result.Success(result))
            }
            .replay(1)
            .autoConnect()
    }
    
    open fun onNetworkResult(networkData: NetworkType) {}

    fun fetch(arg: NetworkArg) {
        fetchProcessor.onNext(arg)
    }

    override fun subscribe(s: Subscriber<in Result<LocalType>>) {
        flow.subscribe(s)
    }
}

inline fun <reified LocalType, reified NetworkType> ResultPublisher<LocalType, Unit, NetworkType>.fetch() {
    fetch(Unit)
}
