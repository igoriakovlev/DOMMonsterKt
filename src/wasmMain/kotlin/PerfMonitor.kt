external class PerfMonitor {
    fun startFPSMonitor()
    fun startMemMonitor()
    fun initProfiler(s: String)
    fun startProfile(s: String)
    fun endProfile(s: String)
}

external val perfMonitor: PerfMonitor