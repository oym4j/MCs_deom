/**
 * 
 */
package com.mingsoft.base.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.mingsoft.base.constant.SessionConst;
import com.mingsoft.basic.action.BaseAction;
import com.mingsoft.basic.biz.IDiyFormBiz;
import com.mingsoft.util.PageUtil;
import com.mingsoft.util.StringUtil;


/**
 * 通用自定义表单
 * @author 欧阳明  QQ 2471844480
 *
 * @date 2016年4月25日 下午3:33:08
 * 类说明   
 * @version 1.0
 */
//http://git.oschina.net/languages/Java 查看Java项目
@Controller("webDiyForm")
@RequestMapping("/from")
public class DiyFormAction extends BaseAction {
			/**
			 * 自定义表单业务处理层
			 */
		@Autowired
		 IDiyFormBiz diyFormBiz;
		/**
		 * 保存
		 * @param idBase64 Base64编码数据
		 * @param request HttpServletRequest对象
		 * @param response HttpServletReponse对象
		 */
		public void save(@PathVariable("idBase64") String  idBase64,HttpServletRequest request, HttpServletResponse response){
				String tmp =this.decryptByAES(request, idBase64);
				String iscode=request.getParameter("idBase64");
				//在进行自定义表单提交数据中是否需要提交验证码，默认需要提交验证码
				if(StringUtil.isBlank(iscode) || iscode.equals("true")){
					Object obj=this.getSession(request, SessionConst.CODE_SESSION);
					if(obj!=null){
						if(!this.checkRandCode(request)){
							this.outJson(response, null,false);
							return;
						}
					}
				}
				//判断传入的加密数字是否能转换成整形
				if(!StringUtil.isInteger(tmp)){
					this.outJson(response, null, false);
					return;
				}
				//获取表单ID
				int formId=Integer.parseInt(tmp);
				if(formId!=0){
					LOG.debug("formId"+formId);
					diyFormBiz.saveDiyFormData(formId, assemblyRequestMap(request));
					this.outJson(response, null,true);
				}
			
		}
		/**
		 * 提供前端查询自定义表单提交数据
		 * @param idBase64  Base64编码数据
		 * @param request
		 * @param response
		 */
		@RequestMapping("{idBase64/queryData}")
		@ResponseBody
		public void  queryData(@PathVariable("idBase64") String idBase64,HttpServletRequest request,HttpServletResponse response  ){
			String temp =this.decryptByAES(request, idBase64);
			int formId=Integer.parseInt(temp);
			//
			if(!StringUtil.isInteger(temp)){
				this.outJson(response, null,false);
				return;
			}
			if(formId!=0){
				int appId=this.getAppId(request);
				int pageNo=this.getPageNo(request);
				//每页显示数量
				int pageSize=this.getInt(request, "pageSize",10);
				  int count =diyFormBiz.countDiyFormData(formId, appId);
					
				//提交记录总数
				PageUtil page=new PageUtil(pageNo,pageSize,count ,"");
				Map map=diyFormBiz.queryDiyFormData(formId,appId, page);
				if(map!=null){
					 	if(map.get("list") !=null){
					 		this.outJson(response, JSON.toJSON("list"));
					 		return;
					 	}
				}
				 this.outString(response, null);
			}
		}
	
}
