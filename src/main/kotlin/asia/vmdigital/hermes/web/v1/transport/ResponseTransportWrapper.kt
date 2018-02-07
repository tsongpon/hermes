package asia.vmdigital.hermes.web.v1.transport

class ResponseTransportWrapper<E> {
    var result: List<E> = ArrayList()
    var next: String? = null
    var previous: String? = null
    var first: String? = null
    var last: String? = null
}