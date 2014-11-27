package org.covito.cas.client.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.covito.cas.client.UrlMatcher;

/**
 * 正则匹配URL
 */
public class RegexUrlMatcher implements UrlMatcher {

	private Set<Pattern> pattern=new HashSet<Pattern>();
	
	@Override
	public boolean matches(String url) {
		boolean matcher=false;;
		for(Pattern p:pattern){
			if(p.matcher(url).find()){
				matcher=true;
				break;
			};
		}
		return matcher;
	}

	@Override
	public void setPattern(String[] pattern) {
		for(String p:pattern){
			this.pattern.add(Pattern.compile(p));
		}
	}

}
