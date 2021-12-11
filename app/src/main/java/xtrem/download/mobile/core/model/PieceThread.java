

package xtrem.download.mobile.core.model;

import xtrem.download.mobile.core.model.data.PieceResult;

import java.util.concurrent.Callable;

interface PieceThread extends Callable<PieceResult> {}
