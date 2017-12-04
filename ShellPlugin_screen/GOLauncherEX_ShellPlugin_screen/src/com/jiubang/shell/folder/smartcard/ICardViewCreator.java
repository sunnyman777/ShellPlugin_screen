package com.jiubang.shell.folder.smartcard;

import java.util.List;

import com.jiubang.shell.folder.smartcard.data.CardBuildInfo;

/**
 * 
 * @author guoyiqing
 *
 */
public interface ICardViewCreator {

	public List<GLAbsCardView> creat(CardBuildInfo buildData);
	
}
