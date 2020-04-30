
//

#include "EncoderNode.h"

EncoderNode::EncoderNode() {}

EncoderNode::~EncoderNode() {
    if (!&(mHandle)) {
        aacEncClose(&(mHandle));
        mHandle = nullptr;
    }
}
