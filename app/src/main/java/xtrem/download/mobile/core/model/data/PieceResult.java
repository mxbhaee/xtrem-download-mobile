
package xtrem.download.mobile.core.model.data;

import java.util.UUID;

public class PieceResult
{
    public UUID infoId;
    public int pieceIndex;
    public long retryAfter;

    public PieceResult(UUID infoId, int pieceIndex)
    {
        this.infoId = infoId;
        this.pieceIndex = pieceIndex;
    }
}
