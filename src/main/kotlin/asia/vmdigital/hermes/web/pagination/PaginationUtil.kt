package asia.vmdigital.hermes.web.pagination

class PaginationUtil {
    companion object {
        fun composePagination(requestedUri: String, page: Int, size: Int, totalSize: Int): Pagination {
            val lastPageNumber: Int? = if (totalSize % size == 0) {
                totalSize / size
            } else {
                totalSize / size + 1
            }

            var nextUri: String? = null
            if (page < lastPageNumber!!) {
                nextUri = requestedUri + "?page=" + (page + 1) + "&size=" + size
            }
            var previousUri: String? = null
            if (page > 1) {
                previousUri = requestedUri + "?page=" + (page - 1) + "&size=" + size
            }
            var firstUri: String? = null
            var lastUri: String? = null
            if (totalSize > 0) {
                firstUri = requestedUri + "?page=" + 1 + "&size=" + size
                lastUri = "$requestedUri?page=$lastPageNumber&size=$size"
            }

            return Pagination(firstUri, lastUri, previousUri, nextUri)
        }
    }
}