package xyz.chrissie.mvrx

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.*
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.concurrent.TimeUnit

data class HelloWorldState(
    val title: String = "Hello World",
    @PersistState val count: Int = 0,
    val temperature: Async<Int> = Uninitialized
) : MvRxState {
    val excitedTitle = "$title $count"
}

class HelloWorldViewModel(initialState: HelloWorldState) : MVRXViewModel<HelloWorldState>(initialState) {
    fun incrementcount() = setState { copy(count = count + 1) }
    fun fetchTemperature() {
        Observable.just(72)
            .delay(3, TimeUnit.SECONDS)
            .execute { copy(temperature = it) }
    }
}

class MainFragment : BaseMvRxFragment() {

    private val viewModel: HelloWorldViewModel by fragmentViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* using your own subscriptions: subscribe, select Subscribe, asyncSubscribe*/
        viewModel.subscribe { state ->
            Log.d(TAG, "The state is $state")
        }

        viewModel.selectSubscribe(HelloWorldState::temperature) { temperature ->
            Log.d(TAG, "The Temp is $temperature")

        }

        viewModel.asyncSubscribe(HelloWorldState::temperature, onSuccess = { temperature ->
            Log.d(TAG, "The Temp is $temperature")
        }, onFail = { error ->
            Log.e(TAG, "An error occured", error)

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleView.setOnClickListener {
            viewModel.fetchTemperature()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }


    override fun invalidate() = withState(viewModel) { state ->
        titleView.text = when (state.temperature) {
            is Uninitialized -> "Click to load weather"
            is Loading -> "Loading"
            is Success -> "Weather: ${state.temperature()} degrees"
            is Fail -> "Failed to load weather"
        }

    }


    companion object {
        private const val TAG = "Main Fragment"
    }
}
