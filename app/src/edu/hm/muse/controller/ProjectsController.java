package edu.hm.muse.controller;

import edu.hm.muse.exception.SuperFatalAndReallyAnnoyingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.springframework.web.util.WebUtils.getCookie;

@Controller
public class ProjectsController {

    @Autowired
    private LoginHelper loginHelper;

    private JdbcTemplate jdbcTemplate;

    @Resource(name = "dataSource")
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
	
	// Load Projects
	@RequestMapping(value = "/projects.secu", method = RequestMethod.GET)
	public ModelAndView getProjectsByUserId(HttpSession session, HttpServletRequest request){
		
        if ((null == session) || (null == session.getAttribute("login")) || ((Boolean) session.getAttribute("login") == false)) {
            return new ModelAndView("redirect:login.secu");
        }
        if (loginHelper.isNotLoggedIn(request, session)) {
            return new ModelAndView("redirect:login.secu");
        }

        Cookie cookie = getCookie(request, "loggedIn");
      //ToDo Auslagern
        String uname = (String) session.getAttribute("user");
        String sql_id = String.format("select ID from M_USER where muname = '%s'", uname);
		int UserIDFromSessionOverDatabase = jdbcTemplate.queryForInt(sql_id);
		
        String sql = "SELECT id, documentname FROM LatexDocuments WHERE muser_id = ?";
        List<Map<String,Object>> projectnames = jdbcTemplate.queryForList(sql, UserIDFromSessionOverDatabase);
        
        ModelAndView mv = new ModelAndView("projects");
        
        mv.addObject("ProjectsForView", projectnames);
        mv.addObject("isLoggedIn", cookie.getValue().equals(session.getAttribute("usertoken")));
        
        return mv;
        
	}
	
	// New Project
	@RequestMapping(value = "/newdocument.secu", method = RequestMethod.GET)
	public ModelAndView saveNewProject(
			@RequestParam(value = "documentname", required = true) String documentname,
			HttpSession session){
	
    if ((null == session) || (null == session.getAttribute("login")) || ((Boolean) session.getAttribute("login") == false)) {
        return new ModelAndView("redirect:login.secu");
    }
    
    //ToDo Auslagern
    String uname = (String) session.getAttribute("user");
    String sql_id = String.format("select ID from M_USER where muname = '%s'", uname);
	int UserIDFromSessionOverDatabase = jdbcTemplate.queryForInt(sql_id);
    
	//Select the Last ID from the Table

        //TODO: Wurde gefixed
	String sqlSelectForDocumentID = "SELECT MAX(id) from LatexDocuments";
            //"SELECT id FROM LatexDocuments ORDER BY id DESC LIMIT 1";
       int ProjectId = jdbcTemplate.queryForInt(sqlSelectForDocumentID);
    //Increment the last ID
    int nextProjectId = ProjectId++;
        
    String sqlContent = String.format("INSERT INTO LatexDocuments (id, muser_id, documentname) VALUES (%s, %s, '%s');", nextProjectId, UserIDFromSessionOverDatabase, documentname);

    int resContent = 0;
    try {
    	//execute the query and check exceptions
    	resContent = jdbcTemplate.update(sqlContent);
    } catch (DataAccessException e) {
        throw new SuperFatalAndReallyAnnoyingException(String.format("Sorry but %sis a bad grammar or has following problem %s ", sqlContent, e.getMessage()));
    }
    
    ModelAndView mv = new ModelAndView("redirect:projects.secu");
    
    return mv;
    
}


    private boolean isNotLoggedIn(HttpServletRequest request, HttpSession session) {
        Cookie cookie = getCookie(request, "loggedIn");
        if (cookie == null) {
            return true;
        }
        if (cookie.getValue().equals(session.getAttribute("usertoken"))) {
            return false;
        }
        return true;
    }


}
