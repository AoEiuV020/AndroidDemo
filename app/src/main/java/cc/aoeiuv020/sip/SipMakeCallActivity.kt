package cc.aoeiuv020.sip

import android.content.Context
import android.net.sip.SipAudioCall
import android.net.sip.SipProfile
import android.net.sip.SipSession
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.R
import cc.aoeiuv020.anull.notNull
import kotlinx.android.synthetic.main.activity_sip_make_call.*
import org.jetbrains.anko.*

class SipMakeCallActivity : AppCompatActivity(), AnkoLogger {
    companion object {
        fun start(ctx: Context, peerProfile: SipProfile) {
            ctx.startActivity<SipMakeCallActivity>(
                    "peerProfile" to peerProfile.uriString
            )
        }

    }

    private lateinit var sipAudioCall: SipAudioCall

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sip_make_call)

        info { "intent: $intent" }
        info { "bundle: ${intent.extras}" }
        bundleOf()

        val sipManager = SipHelper.getSipManager(this)
        val mySipProfile = SipHelper.getMySipProfile(ctx).notNull()
        val peerProfile = intent.getStringExtra("peerProfile").notNull()

        sipAudioCall = sipManager.makeAudioCall(mySipProfile.uriString, peerProfile, object : SipAudioCall.Listener() {
            override fun onCallEstablished(call: SipAudioCall) {
                super.onCallEstablished(call)
                call.run {
                    startAudio()
                    setSpeakerMode(true)
                    if (isMuted) {
                        toggleMute()
                    }
                }
            }

            // 对方没接直接挂断的情况，
            override fun onCallBusy(call: SipAudioCall) {
                super.onCallBusy(call)
                closeSip()
            }

            // 接通后对方挂断的情况，
            override fun onCallEnded(call: SipAudioCall) {
                super.onCallEnded(call)
                closeSip()
            }

            override fun onChanged(call: SipAudioCall) {
                info {
                    "${Thread.currentThread().stackTrace[3].methodName}, " +
                            "state: ${SipSession.State.toString(call.state)}"
                }
            }
        }, 30)

        btnHangUp.setOnClickListener {
            closeSip()
        }
    }

    private fun closeSip() {
        sipAudioCall.endCall()
        sipAudioCall.close()
        finish()
    }
}
