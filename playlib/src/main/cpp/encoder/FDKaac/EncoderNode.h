
//

#ifndef QMUSIC_ENCODERNODE_H
#define QMUSIC_ENCODERNODE_H

#include "libAACenc/include/aacenc_lib.h"

class EncoderNode {
public:
    EncoderNode();

    virtual ~EncoderNode();

    HANDLE_AACENCODER mHandle;
    AACENC_InfoStruct mInfo;
    int mInputSize;
    int mChannels;
};


#endif //QMUSIC_ENCODERNODE_H
