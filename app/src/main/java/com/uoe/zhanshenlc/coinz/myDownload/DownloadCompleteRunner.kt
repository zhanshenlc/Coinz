package com.uoe.zhanshenlc.coinz.myDownload

object DownloadCompleteRunner : DownloadCompleteListener {

    var result: String? = null
    override fun downloadComplete(result: String) {
        DownloadCompleteRunner.result = result
    }
}
