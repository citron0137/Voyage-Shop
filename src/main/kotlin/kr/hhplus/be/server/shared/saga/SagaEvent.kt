class SagaEvent<T> (
    val sagaType: String,
    val eventType: String,
    val intent: String,
    val compensating: Boolean,
    val payload: T
)

