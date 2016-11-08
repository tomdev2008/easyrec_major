/**Copyright 2010 Research Studios Austria Forschungsgesellschaft mBH
 *
 * This file is part of easyrec.
 *
 * easyrec is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * easyrec is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with easyrec.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.easyrec.controller;


import org.easyrec.store.dao.web.OperatorDAO;
import org.easyrec.util.core.Security;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.easyrec.model.core.web.Operator;
import org.easyrec.model.core.web.RemoteTenant;
import org.easyrec.service.web.ViewInitializationService;


/**
 * This Controller displays static content of the homepage.
 * <p/>
 * <p><b>Company:&nbsp;</b>
 * SAT, Research Studios Austria</p>
 * <p/>
 * <p><b>Copyright:&nbsp;</b>
 * (c) 2009</p>
 * <p/>
 * <p><b>last modified:</b><br/>
 * $Author: fsalcher $<br/>
 * $Date: 2012-03-19 14:22:17 +0100 (Mo, 19 Mär 2012) $<br/>
 * $Revision: 18781 $</p>
 *
 * @author dmann
 * @version 1.0
 * @since 1.0
 */
public class HomeController extends MultiActionController {

    private OperatorDAO operatorDAO;
    private ViewInitializationService viewInitializationService;

    public void setOperatorDAO(OperatorDAO operatorDAO) {
        this.operatorDAO = operatorDAO;
    }

    public void setViewInitializationService(ViewInitializationService viewInitializationService) {
        this.viewInitializationService = viewInitializationService;
    }
    
    public ModelAndView robots(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        ModelAndView mav = new ModelAndView("robots");
        return mav;
    }

    public ModelAndView home(HttpServletRequest request, HttpServletResponse httpServletResponse) {

        ModelAndView mav = new ModelAndView("page");
        mav.addObject("title", "easyrec :: home ");
        mav.addObject("page", "home");
        mav.addObject("selectedMenu", "home");
        mav.addObject("signedInOperator", Security.signedInOperator(request));
        mav.addObject("updateToken", Math.random()) ;

        return mav;
    }

    public ModelAndView API(HttpServletRequest request, HttpServletResponse httpServletResponse) {

        ModelAndView mav = new ModelAndView("page");
        mav.addObject("title", "easyrec :: api");
        mav.addObject("page", "api");
        mav.addObject("selectedMenu", "api");
        RemoteTenant remoteTenant = viewInitializationService.initializeView(request, mav);
        return mav;
    }
    
    public ModelAndView apitest(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("apitest/apitest");
        RemoteTenant remoteTenant = viewInitializationService.initializeView(request, mav);
        Operator operator = Security.signedInOperator(request);
        return mav;
    }
    
    public ModelAndView apitestprofile(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("apitest/apitestprofile");
        RemoteTenant remoteTenant = viewInitializationService.initializeView(request, mav);
        return mav;
    }
        
    public ModelAndView contact(HttpServletRequest request, HttpServletResponse httpServletResponse) {

        ModelAndView mav = new ModelAndView("page");
        mav.addObject("title", "easyrec :: contact us");
        mav.addObject("page", "contact");
        return mav;
    }

    public ModelAndView about(HttpServletRequest request, HttpServletResponse httpServletResponse) {

        ModelAndView mav = new ModelAndView("page");
        mav.addObject("title", "easyrec :: about");
        mav.addObject("page", "about");
        return mav;
    }
}
