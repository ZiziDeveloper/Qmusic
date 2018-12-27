//
// Created by Administrator on 2018/12/15 0015.
//

#include "AudioProccessor.h"

AudioProccessor::AudioProccessor() {
    pAudioCoder = new AudioCoder();
    pAudioPlayer = new AudioPlayer();
}

AudioProccessor::~AudioProccessor() {

}

void AudioProccessor::prepare() {
    if (NULL != pAudioCoder) {
        pAudioCoder->prepare();
    }
    if (NULL != pAudioPlayer) {
        pAudioPlayer->prepare();
    }
}

void AudioProccessor::start() {

}

void AudioProccessor::pause() {

}

void AudioProccessor::resume() {

}

void AudioProccessor::stop() {

}

void AudioProccessor::seek(int64_t second) {

}

void AudioProccessor::setVolume(int percent) {

}

void AudioProccessor::switchChannel(int channel) {

}

void AudioProccessor::setPitch(float pitch) {

}

void AudioProccessor::setSpeed(float speed) {

}
