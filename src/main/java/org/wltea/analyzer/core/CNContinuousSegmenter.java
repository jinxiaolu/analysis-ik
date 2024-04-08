package org.wltea.analyzer.core;

import org.wltea.analyzer.dic.Hit;

import java.util.LinkedList;
import java.util.List;

class CNContinuousSegmenter implements ISegmenter{

	//子分词器标签
	static final String SEGMENTER_NAME = "CN_CONTINUOUS_SEGMENTER";

	/*
	 * 词元的开始位置，
	 * 同时作为子分词器状态标识
	 * 当start > -1 时，标识当前的分词器正在处理字符
	 */
	private int nStart;
	/*
	 * 记录词元结束位置
	 * end记录的是在词元中最后一个出现的合理的数词结束
	 */
	private int nEnd;

	//待处理的量词hit队列
	private List<Hit> countHits;


	CNContinuousSegmenter(){
		nStart = -1;
		nEnd = -1;
		this.countHits  = new LinkedList<Hit>();
	}
	
	/**
	 * 分词
	 */
	public void analyze(AnalyzeContext context) {
		this.processCN(context);

		//判断是否锁定缓冲区
		if(this.nStart == -1 && this.nEnd == -1	&& countHits.isEmpty()){
			//对缓冲区解锁
			context.unlockBuffer(SEGMENTER_NAME);
		}else{
			context.lockBuffer(SEGMENTER_NAME);
		}
	}
	

	/**
	 * 重置子分词器状态
	 */
	public void reset() {
		nStart = -1;
		nEnd = -1;
		countHits.clear();
	}

	private void processCN(AnalyzeContext context){
		if(nStart == -1 && nEnd == -1){//初始状态
			if(CharacterUtil.CHAR_CHINESE == context.getCurrentCharType()){
				//记录起始、结束位置
				nStart = context.getCursor();
				nEnd = context.getCursor();
			}
		}else{//正在处理状态
			if(CharacterUtil.CHAR_CHINESE == context.getCurrentCharType()){
				//记录数词的结束位置
				nEnd = context.getCursor();
			}else{
				//输出数词
				this.outputLexeme(context);
				//重置头尾指针
				nStart = -1;
				nEnd = -1;
			}
		}

		//缓冲区已经用完，还有尚未输出的数词
		if(context.isBufferConsumed() && (nStart != -1 && nEnd != -1)){
			//输出数词
			outputLexeme(context);
			//重置头尾指针
			nStart = -1;
			nEnd = -1;
		}
	}

	private void outputLexeme(AnalyzeContext context){
		if(nStart > -1 && nEnd > -1){
			//输出数词
			Lexeme newLexeme = new Lexeme(context.getBufferOffset() , nStart , nEnd - nStart + 1 , Lexeme.TYPE_CNWORD);
			context.addLexeme(newLexeme);
			
		}
	}

}
