
//

#ifndef QMUSIC_ENCODERNODE_H
#define QMUSIC_ENCODERNODE_H

#include "libAACenc/include/aacenc_lib.h"

class EncoderNode {
public:
    EncoderNode();

    virtual ~EncoderNode();

    HANDLE_AACENCODER mHandle{nullptr};
    AACENC_InfoStruct mInfo{0};
    int mInputSize{0};
    int mChannels{0};
};


#endif //QMUSIC_ENCODERNODE_H
