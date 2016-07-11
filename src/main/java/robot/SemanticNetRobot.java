package robot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.dom4j.DocumentException;

import edu.uestc.robot.ann.BestAnswer;
import edu.uestc.robot.nlp.roughlymatch.AnswerBean;
import edu.uestc.robot.nlp.roughlymatch.Matches;
import edu.uestc.robot.nlp.roughlymatch.QuestionBean;
import edu.uestc.robot.nlp.roughlymatch.RoughlyMatch;

public class SemanticNetRobot implements Robot {
	HashMap<String, String> contentMapReply;
	private static SemanticNetRobot semanticNetRobot;

	private SemanticNetRobot(){
		contentMapReply = new HashMap<String, String>();
		init();
	}

	private void init(){
		contentMapReply.put("您好", "您好！我是万国置地在线客服，请问有什么可以帮到您?");
	}
	
	public static SemanticNetRobot getInstance(){
		if(semanticNetRobot == null){
			synchronized(SemanticNetRobot.class){
				if(semanticNetRobot == null){
					semanticNetRobot = new SemanticNetRobot();
				}
			}
		}
		return semanticNetRobot;
	}
	
	@SuppressWarnings("unused")
	@Override
	public String getReply(String content, HttpSession userSession) throws IOException {
		// TODO Auto-generated method stub
		
		/**
		 * 一：得到上一个问题的本体，并保存语义网络需要的数据
		 */
		List<String> lastOntology = (List<String>) userSession.getAttribute("lastOntology");
		{
			RoughlyMatch roughlyMathc = new RoughlyMatch();
			QuestionBean questionBean = new QuestionBean("", new ArrayList<String>());
			ArrayList<AnswerBean> answerBeans = new ArrayList<AnswerBean>();
			try {
				Matches matches = roughlyMathc.roughlyMatch(content, questionBean, answerBeans);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			List<String> ontology = questionBean.getQOntology();
			userSession.setAttribute("lastOntology", ontology);			
		}
		
		/**
		 * 二：调用杨彬自然语言接口，得到模糊答案对象
		 */
		RoughlyMatch roughlyMathc = new RoughlyMatch();
		QuestionBean questionBean = new QuestionBean("", new ArrayList<String>());
		ArrayList<AnswerBean> answerBeans = new ArrayList<AnswerBean>();
		try {
			Matches matches = roughlyMathc.roughlyMatch(content, questionBean, answerBeans);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		/**
		 * 三：调用程强机器学习接口，将模糊答案对象传递给这个接口，得到最优答案
		 */
		String result = "";
		if(answerBeans.size() == 0){
			return "请问您的微信号是？因为我这边是在线客服 信息掌握的不是很全面 可以安排专业顾问给您详细推荐一下"; 
		}
		else{
			if(lastOntology != null){
//				try {
//					Matches matches = roughlyMathc.roughlyMatch(content, questionBean, answerBeans);
//					List<AnswerBean> answerBeanList = BestAnswer.getBestAnswerArrayList(questionBean, answerBeans, 0.2d);
//					result = getAnswer(lastOntology, answerBeanList);
//				} catch (DocumentException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				List<AnswerBean> answerBeanList = BestAnswer.getBestAnswerArrayList(questionBean, answerBeans, 0.5d);
				result = getAnswer(lastOntology, answerBeanList);
			}
			else{
				result = BestAnswer.getBestAnswerString(questionBean, answerBeans);
//				try {
//					Matches matches = roughlyMathc.roughlyMatch(content, questionBean, answerBeans);
//					result = BestAnswer.getBestAnswerString(questionBean, answerBeans);
//				} catch (DocumentException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}
			
		/**
		 * 四：如果最优答案为""，则调用刘爽黄为的主义网络，构造答案
		 */
		
		/**
		 * 五：将答案返回
		 */
		if(result.length() == 0){
			return "您好！您可以留下您的联系方式 稍后我这边会转接到相关部门 如果有需要会给您打电话";
		}
		else{
			return result;
		}
	}


	
	private String getAnswer(List<String> lastOntology, List<AnswerBean> answerBeanList){
		int countMax=0;
		int index = 0;
		for(int loop=0; loop<answerBeanList.size(); loop++){
			int count = 0;
			for(String ontology : lastOntology){
				if(answerBeanList.get(loop).getAOntology().contains(ontology)){
					count++;
				}
			}
			if(count > countMax){
				countMax = count;
				index = loop;
			}
		}
		return answerBeanList.get(index).getAStr();
	}
}
