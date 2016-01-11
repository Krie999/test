package io.fourcast.gae.service;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.services.admin.directory.model.Member;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import io.fourcast.gae.dao.*;
import io.fourcast.gae.dao.comms.CommsProjectDao;
import io.fourcast.gae.manager.UserManager;
import io.fourcast.gae.model.cocamat.CocamatCodeGenerator;
import io.fourcast.gae.model.cocamat.CocamatProject;
import io.fourcast.gae.model.comms.CommsProject;
import io.fourcast.gae.model.lmd.LmdTask;
import io.fourcast.gae.model.smart.SmartProject;
import io.fourcast.gae.model.space.SpaceProject;
import io.fourcast.gae.model.user.DSUser;
import io.fourcast.gae.service.dto.fieldOption.FieldOptionList;
import io.fourcast.gae.utils.Globals;
import io.fourcast.gae.utils.exceptions.ConstraintViolationsException;
import io.fourcast.gae.utils.exceptions.E2EServerException;
import io.fourcast.gae.utils.exceptions.E2EUserInputException;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nielsbuekers on 07/08/15.
 */
@Api(
        name = "adminService",
        version = "v0.0.1",
        description = "Service to handle Admin Ops",
        clientIds = {
                com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID}
)
public class AdminService extends AbstractService {

    private UserDao userDao = new UserDao();
    private CocamatDao cocamatDao = new CocamatDao();
	private SmartDao smartDao = new SmartDao();
	private SpaceDao spaceDao = new SpaceDao();
	private LmdDao lmdDao = new LmdDao();
    private CommsProjectDao commsProjectDao = new CommsProjectDao();
	//private FieldOptionTranslator foTranslator = new FieldOptionTranslator();
	
    private FieldOptionDao fieldOptionDao = new FieldOptionDao();
    private ChannelOptionDao channelOptionDao = new ChannelOptionDao();
    private UserManager userManager = new UserManager();

    private SearchManager searchManager = new SearchManager();

    private static final String CONFIRM_CODE = "ja ik ben zeker";
    private static final String CONFIRM_ERROR = "nieje joenge. nieje.";
    private Gson gson = new Gson();

    @ApiMethod(name = "ping")
    public void Ping(){
        return;
    }

    @ApiMethod(name = "deleteIndexId")
    public void deleteIndexId(User user,@Named("id")String id,@Named("confirm")String confirm) throws OAuthRequestException, UnauthorizedException, E2EUserInputException {
        validateUserAccess(user);
        if (confirm == null || !confirm.equals(CONFIRM_CODE)) {
            throw new E2EUserInputException(CONFIRM_ERROR);
        }
        SearchManager mgr = new SearchManager();
        mgr.deleteDocWithId(id);
    }

    @ApiMethod(name = "updateCocamatCodeGenerator")
    public CocamatCodeGenerator updateCocamatCodeGenerator(User user, CocamatCodeGenerator generator, @Named("confirm") String confirm) throws OAuthRequestException, UnauthorizedException, E2EUserInputException {
        validateUserAccess(user);
        if (confirm == null || !confirm.equals(CONFIRM_CODE)) {
            throw new E2EUserInputException(CONFIRM_ERROR);
        }
        return cocamatDao.updateCocamatCodeGenerator(generator);
    }
    
    @ApiMethod(name = "indexAllExistingProjects")
    public void indexAllExistingProjects(User user, @Named("confirm") String confirm, @Named("appType") Globals.APP appType) throws E2EServerException, ConstraintViolationsException, ParseException{
    	switch(appType){
    	  case APP_SMART:
    		  List<SmartProject> smartProjects = smartDao.getAllSmartProjects();
    		  for(SmartProject project : smartProjects){
    	    		smartDao.addProjectToSearchDS(project);
    	      }
    		  break;
    	  case APP_COCAMAT:
    		  List<CocamatProject> cocamatProjects = cocamatDao.getAllCocamatProjects();
    		  for(CocamatProject project : cocamatProjects){
    	    		cocamatDao.addProjectToSearchDS(project);
    	      }
    		  break;
    	  case APP_SPACE:
    		  List<SpaceProject> spaceProjects = spaceDao.getSpaceProjects();
    		  for(SpaceProject project : spaceProjects){
    	    		spaceDao.addProjectToSearchDS(project);
    	      }
    		  break;
    	  case APP_LMD:
    		  List<LmdTask> lmdTasks = lmdDao.getAllLmdTasks();
    	      for(LmdTask task : lmdTasks){
    	    		lmdDao.addLmdTaskToSearchDS(task);
    	      }
    	      break;
    	  case APP_CHANNELS:
    		  //TODO in sprint for channels
    		  break;
    	  case APP_COMMS:
              List<CommsProject> commsProjects = commsProjectDao.getAllCommsProjects();
              for(CommsProject project: commsProjects) {
                  commsProjectDao.addProjectToSearchDS(project);
              }
    		  break;
    	  case APP_ALL:
    		  List<SmartProject> smarts = smartDao.getAllSmartProjects();
    		  for(SmartProject project : smarts){
    	    		smartDao.addProjectToSearchDS(project);
    	      }
    		  
    		  List<CocamatProject> cocamats = cocamatDao.getAllCocamatProjects();
    		  for(CocamatProject project : cocamats){
    			  	cocamatDao.addProjectToSearchDS(project);
    	      }
    	      
    		  List<SpaceProject> spaces = spaceDao.getSpaceProjects();
    		  for(SpaceProject project : spaces){
    	    		spaceDao.addProjectToSearchDS(project);
    	      }
    		  
    		  List<LmdTask> tasks = lmdDao.getAllLmdTasks();
    	      for(LmdTask task : tasks){
    	    		lmdDao.addLmdTaskToSearchDS(task);
    	      }
              List<CommsProject> comms = commsProjectDao.getAllCommsProjects();
              for(CommsProject project: comms) {
                  commsProjectDao.addProjectToSearchDS(project);
              }
    	      break;
    	} 	
    }

    /**
     * Loads all unknown users that are member from the group e2e_all_users@bnpparibasfortis.com into the DS.
     * For the known users, the TS is NOT validated. If they need a re-fetch, the user should trigger 'getUserDetails'
     * or the users should be removed from the DS, and then this function needs to be run again.
     *
     * @return
     * @throws IOException
     * @throws E2EServerException
     * @throws ConstraintViolationsException
     */
    @ApiMethod(name = "loadAllUnknownUsers")
    public List<DSUser> loadAllUnknownUsers(User user) throws IOException, E2EServerException, ConstraintViolationsException, OAuthRequestException, UnauthorizedException {
        user = validateUserAccess(user);
        //get all members from the groups
        List<Member> allMembers = userManager.getGroupMembersForGroup(Globals.GAPPS_GROUP_ALL);
        List<String> emailsToLoad = new ArrayList<>();

        //for each member, check if in DS and valid
        for (Member m : allMembers) {
            DSUser dsUser = userDao.getUserByEmail(m.getEmail());
            if (dsUser == null) {
                emailsToLoad.add(m.getEmail());
                log.info("new user " + m.getEmail() + " found.");
            }
        }
        int count = 0;
        List<DSUser> savedUsers = new ArrayList<>();
        // get user details
        List<DSUser> remoteUsers = new ArrayList<>();
        for (String emailtoLoad : emailsToLoad) {
            DSUser remoteUser = userManager.getRemoteUserDetails(emailtoLoad);
            remoteUsers.add(remoteUser);
            count++;
            if(count % 100 == 0){
                List<DSUser> saved = userDao.saveUsers(remoteUsers);
                savedUsers.addAll(saved);
                remoteUsers.clear();
                break;
            }
        }
        //save last batch < 100
        if(remoteUsers.size() > 0){
            List<DSUser> saved = userDao.saveUsers(remoteUsers);
            savedUsers.addAll(saved);
        }
        //store details
        return savedUsers;
    }

    @ApiMethod(name = "resetallData")
    public void resetallData(User user,@Named("confirm") String confirm) throws E2EServerException, OAuthRequestException, UnauthorizedException, IOException, ConstraintViolationsException, E2EUserInputException {
        validateUserAccess(user);
        if (confirm == null || !confirm.equals(CONFIRM_CODE)) {
            throw new E2EUserInputException(CONFIRM_ERROR);
        }

        //clear search
        searchManager.unIndexAllDocuments("E2E");
        //clear users
        userDao.deleteAllUsers();
        //reset FO
        resetFieldOptions(user,confirm);
        //reset CO
        resetChannelOptions(user,confirm);
        //reload all users
        loadAllUnknownUsers(user);

    }

    @ApiMethod(name = "cleanSearchIndex")
    public void CleanSearchIndex(User user, @Named("confirm") String confirm) throws UnauthorizedException, OAuthRequestException, E2EUserInputException {
        user = validateUserAccess(user);
        if (confirm == null || !confirm.equals(CONFIRM_CODE)) {
            throw new E2EUserInputException(CONFIRM_ERROR);
        }
        searchManager.unIndexAllDocuments("E2E");
    }

    @ApiMethod(name = "resetChannelOptions")
    public List<ChannelOption> resetChannelOptions(User user,@Named("confirm") String confirm) throws OAuthRequestException, UnauthorizedException, UnsupportedEncodingException, E2EUserInputException {
        validateUserAccess(user);

        if (confirm == null || !confirm.equals(CONFIRM_CODE)) {
            throw new E2EUserInputException(CONFIRM_ERROR);
        }

        //delete all
        channelOptionDao.deleteAllChannelOptions();

        //read from JSON
        InputStream is = AdminService.class.getResourceAsStream("/channelOptions.json");
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);

        //convert the json string back to object
        ChannelOptionList channelOptionList = gson.fromJson(reader, ChannelOptionList.class);
        //save fieldOptions
        return channelOptionDao.saveChannelOptions(channelOptionList.getChannelOptions());
    }


    /*
    @ApiMethod(name = "migrateToNewDatamodel", path = "migrateToNewDatamodel")
    public void migrateToNewDatamodel(User user) throws OAuthRequestException, UnauthorizedException, E2EServerException, ConstraintViolationsException {
        validateUserAccess(user);
        //transform replan messages to arrayList
    }
    */


    @ApiMethod(name = "resetFieldOptions")
    public List<FieldOption> resetFieldOptions(User user,@Named("confirm") String confirm) throws OAuthRequestException, UnauthorizedException, UnsupportedEncodingException, ConstraintViolationsException, E2EUserInputException {
        validateUserAccess(user);

        if (confirm == null || !confirm.equals(CONFIRM_CODE)) {
            throw new E2EUserInputException(CONFIRM_ERROR);
        }

        //delete all
        fieldOptionDao.deleteAllFieldOptions();

        /** GENERAL **/
        //read from JSON
        InputStream isGeneral = AdminService.class.getResourceAsStream("/fieldoptions/fieldOptionsGeneral.json");
        InputStreamReader isrGeneral = new InputStreamReader(isGeneral, "UTF-8");
        BufferedReader readerGeneral = new BufferedReader(isrGeneral);

        //read 1 time from file. Copy Fortis to Fintro and HelloBank
        List<FieldOption> fieldOptionsFortis = gson.fromJson(readerGeneral, FieldOptionList.class).getFieldOptions();

        List<FieldOption> fieldOptionsFintro = new ArrayList<>();
        List<FieldOption> fieldOptionsHelloBank = new ArrayList<>();
        List<FieldOption> fieldOptionsHelloFortis = new ArrayList<>();
        List<FieldOption> fieldOptionsFortisHello = new ArrayList<>();

        //set brand and make clones for fintro and hello bank
        for (FieldOption foFortis : fieldOptionsFortis) {

            FieldOption foFintro = (FieldOption) foFortis.clone();
            FieldOption foHelloBank = (FieldOption) foFortis.clone();
            FieldOption foHelloFortis = (FieldOption) foFortis.clone();
            FieldOption foFortisHello = (FieldOption) foFortis.clone();

            foFortis.setBrand(Globals.BRAND.FORTIS);
            foFintro.setBrand(Globals.BRAND.FINTRO);
            foHelloBank.setBrand(Globals.BRAND.HELLO_BANK);
            foHelloFortis.setBrand(Globals.BRAND.HELLO_FORTIS);
            foFortisHello.setBrand(Globals.BRAND.FORTIS_HELLO);

            fieldOptionsFintro.add(foFintro);
            fieldOptionsHelloBank.add(foHelloBank);
            fieldOptionsHelloFortis.add(foHelloFortis);
            fieldOptionsFortisHello.add(foFortisHello);
        }

        /** FORTIS **/
        //get brand specific from file
        FieldOptionList fieldOptionsListFortis = fieldOptionsFromFile("/fieldoptions/fieldOptionsFortis.json");
        //add brand specific to general FO's  (adapted for that brand already)
        fieldOptionsFortis.addAll(fieldOptionsListFortis.getFieldOptions());
        //save combination of general + brand specific (all saved under that same brand)
        fieldOptionDao.saveFieldOptions(fieldOptionsFortis);

        /** FINTRO **/
        FieldOptionList fieldOptionListFintro = fieldOptionsFromFile("/fieldoptions/fieldOptionsFintro.json");
        fieldOptionsFintro.addAll(fieldOptionListFintro.getFieldOptions());
        fieldOptionDao.saveFieldOptions(fieldOptionsFintro);

        /** HELLO_BANK **/
        FieldOptionList fieldOptionListHelloBank = fieldOptionsFromFile("/fieldoptions/fieldOptionsHelloBank.json");
        fieldOptionsHelloBank.addAll(fieldOptionListHelloBank.getFieldOptions());
        fieldOptionDao.saveFieldOptions(fieldOptionsHelloBank);

        /**HELLO Bank + Fortis**/
        FieldOptionList fieldOptionListHelloFortis = fieldOptionsFromFile("/fieldoptions/fieldOptionsHelloFortis.json");
        fieldOptionsHelloFortis.addAll(fieldOptionListHelloFortis.getFieldOptions());
        fieldOptionDao.saveFieldOptions(fieldOptionsHelloFortis);


        /** Fortis + Hello Bank **/
        FieldOptionList fieldOptionListFortisHello = fieldOptionsFromFile("/fieldoptions/fieldOptionsFortisHello.json");
        fieldOptionsFortisHello.addAll(fieldOptionListFortisHello.getFieldOptions());
        fieldOptionDao.saveFieldOptions(fieldOptionsFortisHello);

        List<FieldOption> options = new ArrayList<>();
        options.addAll(fieldOptionsFortis);
        options.addAll(fieldOptionsFintro);
        options.addAll(fieldOptionsHelloBank);
        options.addAll(fieldOptionsHelloFortis);
        options.addAll(fieldOptionsFortisHello);

        return options;
    }

    private FieldOptionList fieldOptionsFromFile(String fileUrl) throws UnsupportedEncodingException {
        InputStream is = AdminService.class.getResourceAsStream(fileUrl);
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        return gson.fromJson(reader, FieldOptionList.class);
    }

    @Override
    protected Globals.USER_ROLE requiredRole() {
        return Globals.USER_ROLE.ROLE_ADMIN;
    }
}
