package com.wjh;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SensitiveUtil {
	public SensitiveUtil(){
		readKeyWordFile("keyword.txt");
	}
	private class TrieNode {
		//是不是关键字的结尾
		private boolean end = false;

		//当前节点下所有的子节点
		private Map<Character,TrieNode> subNodes= new HashMap<>();

		public void addSubNode(Character key,TrieNode node) {
			subNodes.put(key, node);
		}

		public TrieNode getSubNode(Character key) {
			return subNodes.get(key);
		}

		public boolean isKeywordEnd() {
			return end;
		}

		public void setKeywordEnd(boolean end) {
			this.end = end;
		}
	}
	private TrieNode rootNode = new TrieNode();

	public void addWord(String lineText) {
		TrieNode tempNode=rootNode;
		for (int i = 0; i <lineText.length() ; i++) {
			Character c = lineText.charAt(i);
			if(isSymbol(c)){
				continue;
			}
			TrieNode node = tempNode.getSubNode(c);
			if(node == null) {
				node  = new TrieNode();
				tempNode.addSubNode(c,node);
			}
			tempNode = node;
			if(i == lineText.length()-1) {
				tempNode.setKeywordEnd(true);
			}
		}
	}

	public void readKeyWordFile(String path) {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("keyword.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String lineText;
			while((lineText=br.readLine())!=null) {
				addWord(lineText.trim());
			}
		}catch (Exception e){
			System.out.println("读取异常");
		}
	}

	private boolean isSymbol(Character c) {
		int ic = (int)c;
		return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic >0x9FFF);
	}
	public String filter(String text) {
		if(StringUtils.isBlank(text)) {
			return text;
		}
		String replacement = "***";
		StringBuilder result = new StringBuilder();
		TrieNode tempNode = rootNode;
		int begin = 0;
		int position = 0;
		while(position < text.length()) {
			Character c=text.charAt(position);
			if(isSymbol(c)){
				if(tempNode == rootNode) {
					result.append(c);
					++begin;
				}
				++position;
				continue;
			}
			tempNode = tempNode = tempNode.getSubNode(c);
			if (tempNode == null) {
				result.append(text.charAt(begin));
				position = begin + 1;
				begin = position;
				tempNode = rootNode;
			}else if (tempNode.isKeywordEnd()) {
				result.append(replacement);
				position = position + 1;
				begin = position;
				tempNode = rootNode;
			}else {
				++position;
			}
		}
		result.append(text.substring(begin));
		return result.toString().trim();
	}

	public static void main(String[] args) {
		SensitiveUtil s = new SensitiveUtil();
		String text = "唉 你真暴 力            ";
		String filterText = s.filter(text);
		System.out.println(filterText);
	}
}

