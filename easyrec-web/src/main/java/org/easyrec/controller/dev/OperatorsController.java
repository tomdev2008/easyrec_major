/*
 * Copyright 2015 Research Studios Austria Forschungsgesellschaft mBH
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

package org.easyrec.controller.dev;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easyrec.model.core.web.Operator;
import org.easyrec.store.dao.web.OperatorDAO;
import org.easyrec.util.core.MessageBlock;
import org.easyrec.util.core.Security;
import org.easyrec.utils.PageStringGenerator;
import org.easyrec.utils.servlet.ServletUtils;
import org.easyrec.vocabulary.MSG;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author dmann
 */
public class OperatorsController  extends MultiActionController {
    private OperatorDAO operatorDAO;

    public void setOperatorDAO(OperatorDAO operatorDAO) {
        this.operatorDAO = operatorDAO;
    }

    private static final String VIEW_OPERATORS = "viewOperators";


    public ModelAndView viewoperators(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        PageStringGenerator psg = new PageStringGenerator(
                request.getRequestURL() + "?" + request.getQueryString());

        String tenantId = ServletUtils.getSafeParameter(request, "tenantId", "");
        String operatorId = ServletUtils.getSafeParameter(request, "operatorId", "");
        String searchString = ServletUtils.getSafeParameter(request, "searchString", "");


        int siteNumber = ServletUtils.getSafeParameter(request, "siteNumber", 0);

        ModelAndView mav = new ModelAndView("page");

        mav.addObject("title", "easyrec :: administration");

        mav.addObject("operatorId", operatorId);
        mav.addObject("tenantId", tenantId);
        mav.addObject("searchString", searchString);
        mav.addObject("url", request.getRequestURL());

        if (Security.isDeveloper(request)) {
            int operatorsTotal = operatorDAO.count(searchString);
            mav.addObject("operatorsTotal", operatorsTotal);
            mav.addObject("pageMenuString", psg.getPageMenuString(operatorsTotal, siteNumber));

            List<Operator> operators = operatorDAO.getOperators(searchString, siteNumber * psg.getNumberOfItemsPerPage(), psg.getNumberOfItemsPerPage());

            mav.setViewName("dev/page");
            mav.addObject("page", "viewoperators");
            mav.addObject("operators", operators);
            mav.addObject("dbname", operatorDAO.getDbName());

            return mav;
        } else {
            return MessageBlock.createSingle(mav, MSG.NOT_SIGNED_IN, VIEW_OPERATORS, MSG.ERROR);
        }
    }

}
