

package xtrem.download.mobile.core.filter;

import xtrem.download.mobile.core.model.data.entity.InfoAndPieces;

import io.reactivex.functions.Predicate;

public interface DownloadFilter extends Predicate<InfoAndPieces> {}
