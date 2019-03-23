package com.ed.matchandreplace;

import com.ed.matchandreplace.api.Rule;
import com.ed.matchandreplace.api.RuleMatcher;
import com.ed.matchandreplace.api.RulePath;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JSONOperationsTestCase extends MuleArtifactFunctionalTestCase {
    public static final RuleMatcher MATCHER_JOHN = new RuleMatcher(RuleMatcher.Type.STRING, "/text", "john");
    public static final RuleMatcher MATCHER_JOHN_REGEX = new RuleMatcher(RuleMatcher.Type.REGEX, "/text", "j..n");
    public static final RuleMatcher MATCHER_ROGER = new RuleMatcher(RuleMatcher.Type.STRING, "/text", "roger");

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     */
    @Override
    protected String getConfigFile() {
        return "test-mule-config.xml";
    }

    @Test
    public void executeReplaceSSNNodeMatchString() throws Exception {
        String payloadValue = (String) flowRunner("executeReplaceSSNNodeMatchString")
                .run()
                .getMessage()
                .getPayload()
                .getValue();
        assertThat(payloadValue, is("{\"text\":\"john\",\"ssn\":\"SSN-9398-5670-8231-9087\",\"array\":[\"tval1\",\"tval2\",33,true,{\"val\":\"tval2\"}],\"obj\":{\"person\":{\"name\":\"david\",\"ssn\":\"1111-2222-3333-4444\"}},\"bool\":true,\"numb\":243,\"__^tokenizedvalues^__\":[{\"template\":\"vtsUsersTemplate\",\"group\":\"vtsUsers\",\"path\":\"/ssn\"}]}"));
    }

    @Test
    public void testJsonPath() throws Exception {
        String payloadValue = (String) flowRunner("testJsonPath")
                .run()
                .getMessage()
                .getPayload()
                .getValue();
        assertThat(payloadValue, is("{\"request\":{\"path\":\"/api/prc/test/johnsmith/send\",\"arr\":[{\"ssn\":\"KDZS-Ee8T-TgN9-bAfB\"},{\"ssn\":\"s3Qu-29ZM-vVhh-2FcM\"}]},\"__^tokenizedvalues^__\":[{\"template\":\"vttCustomerEmail\",\"group\":\"vtgCustomerPII\",\"path\":\"sp:$.request.arr.*.ssn\"}]}"));
    }

    @Test
    public void executeReplaceSSNNodeMatchRegex() throws Exception {
        String payloadValue = execute("tokenizeFlow", Rule.createList(MATCHER_JOHN_REGEX,createSSNPath("/ssn")));
        assertThat(payloadValue, is("{\"text\":\"john\",\"ssn\":\"SSN-9398-5670-8231-9087\",\"array\":[\"tval1\",\"tval2\",33,true,{\"val\":\"tval2\"}],\"obj\":{\"person\":{\"name\":\"david\",\"ssn\":\"1111-2222-3333-4444\"}},\"bool\":true,\"numb\":243,\"__^tokenizedvalues^__\":[{\"template\":\"vtsUsersTemplate\",\"group\":\"vtsUsers\",\"path\":\"/ssn\"}]}"));
    }

    @Test
    public void executeReplaceSSNNodeNoMatch() throws Exception {
        String payloadValue = execute("tokenizeFlow", Rule.createList(MATCHER_ROGER,createSSNPath("/ssn")));
        assertThat(payloadValue, is("{\"text\":\"john\",\"ssn\":\"1111-2222-3333-4444\",\"array\":[\"tval1\",\"tval2\",33,true,{\"val\":\"tval2\"}],\"obj\":{\"person\":{\"name\":\"david\",\"ssn\":\"1111-2222-3333-4444\"}},\"bool\":true,\"numb\":243,\"__^tokenizedvalues^__\":[]}"));
    }

    @Test
    public void executeReplaceTextNodeMatchDeepObject() throws Exception {
        String payloadValue = execute("tokenizeFlow", Rule.createList(MATCHER_JOHN,createSSNPath(("/obj/person/ssn"))));
        assertThat(payloadValue, is("{\"text\":\"john\",\"ssn\":\"1111-2222-3333-4444\",\"array\":[\"tval1\",\"tval2\",33,true,{\"val\":\"tval2\"}],\"obj\":{\"person\":{\"name\":\"david\",\"ssn\":\"SSN-9398-5670-8231-9087\"}},\"bool\":true,\"numb\":243,\"__^tokenizedvalues^__\":[{\"template\":\"vtsUsersTemplate\",\"group\":\"vtsUsers\",\"path\":\"/obj/person/ssn\"}]}"));
     }

    @Test
    public void testDetokenization() throws Exception {
        String payload = (String) flowRunner("detokenizeFlow")
                .run()
                .getMessage()
                .getPayload()
                .getValue();
        assertThat(payload, is("{\"text\":\"john\",\"ssn\":\"1111-2222-3333-4444\",\"array\":[\"tval1\",\"tval2\",33,true,{\"val\":\"tval2\"}],\"obj\":{\"person\":{\"name\":\"david\",\"ssn\":\"1111-2222-3333-4444\"}},\"bool\":true,\"numb\":243}"));
    }

    private String execute(String flow, List<Rule> rules) throws Exception {
        return (String) flowRunner(flow)
                .withVariable("tokenizationRules", rules)
                .run()
                .getMessage()
                .getPayload()
                .getValue();
    }

    private RulePath createSSNPath(String path) {
        return new RulePath("vtsUsersTemplate","vtsUsers", path);
    }

    @Test
    public void executeReplaceTextNodeMatchNullObject() throws Exception {
        String payloadValue = execute("tokenizeFlowNullPayload", Rule.createList(MATCHER_JOHN,createSSNPath(("/obj/person/ssn"))));
        assertThat(payloadValue, is("{\"text\":\"john\",\"ssn\":"+null+",\"array\":[\"tval1\",\"tval2\",33,true,{\"val\":\"tval2\"}],\"obj\":{\"person\":{\"name\":\"david\",\"ssn\":"+null+"}},\"bool\":true,\"numb\":243,\"__^tokenizedvalues^__\":[{\"template\":\"vtsUsersTemplate\",\"group\":\"vtsUsers\",\"path\":\"/obj/person/ssn\"}]}"));
    }

    @Test
    public void executeReplaceNullArray() throws Exception {
        String payloadValue = execute("tokenizeFlowNullArray", Rule.createList(MATCHER_JOHN,createSSNPath(("sp:$.target.*.alternateId"))));
        assertThat(payloadValue, is("{\"actor\":{\"id\":\"00uf2ciivyzio8ZnZ0h7\",\"type\":\"User\",\"alternateId\":\"muleuserdev@velobank.com\",\"displayName\":\"Mule User\",\"detailEntry\":null},\"client\":{\"userAgent\":{\"rawUserAgent\":\"AHC/1.0\",\"os\":\"Unknown\",\"browser\":\"UNKNOWN\"},\"zone\":\"null\",\"device\":\"Unknown\",\"id\":null,\"ipAddress\":\"52.35.147.213\",\"geographicalContext\":{\"city\":\"Boardman\",\"state\":\"Oregon\",\"country\":\"United States\",\"postalCode\":\"97818\",\"geolocation\":{\"lat\":45.8491,\"lon\":-119.7143}}},\"authenticationContext\":{\"authenticationProvider\":null,\"credentialProvider\":null,\"credentialType\":null,\"issuer\":null,\"interface\":null,\"authenticationStep\":0,\"externalSessionId\":\"trszfCx9W0STJSVtdYvCe7adg\"},\"displayMessage\":\"Update user profile for Okta\",\"eventType\":\"user.account.update_profile\",\"outcome\":{\"result\":\"SUCCESS\",\"reason\":null},\"published\":\"2018-11-26T19:23:24.093Z\",\"securityContext\":{\"asNumber\":null,\"asOrg\":null,\"isp\":null,\"domain\":null,\"isProxy\":null},\"severity\":\"INFO\",\"debugContext\":{\"debugData\":{\"requestId\":\"W-xILP4inm2dx@t856BdaAAABrc\",\"requestUri\":\"/api/v1/users/n1.g@ewb11.com\",\"changedAttributes\":\"CIS_Number,lastName,firstName,mobilePhone\",\"url\":\"/api/v1/users/n1.g@ewb11.com?\"}},\"legacyEventType\":\"core.user.config.profile_update.success\",\"transaction\":{\"type\":\"WEB\",\"id\":\"W-xILP4inm2dx@t856BdaAAABrc\",\"detail\":{}},\"uuid\":\"f5f48b1b-9ba7-464b-ac87-8b046057ce3f\",\"version\":\"0\",\"request\":{\"ipChain\":[{\"ip\":\"52.35.147.213\",\"geographicalContext\":{\"city\":\"Boardman\",\"state\":\"Oregon\",\"country\":\"United States\",\"postalCode\":\"97818\",\"geolocation\":{\"lat\":45.8491,\"lon\":-119.7143}},\"version\":\"V4\",\"source\":null}]},\"target\":null,\"__^tokenizedvalues^__\":[]}"));
    }


    @Test
    public void executeReplaceArray() throws Exception {
        String payloadValue = execute("tokenizeFlowArrayPayload", Rule.createList(MATCHER_JOHN,createSSNPath(("sp:$.request.payload.Customer.Names.*.LastName"))));
        System.out.println(payloadValue);
       // assertThat(payloadValue, is("{\"actor\":{\"id\":\"00uf2ciivyzio8ZnZ0h7\",\"type\":\"User\",\"alternateId\":\"muleuserdev@velobank.com\",\"displayName\":\"Mule User\",\"detailEntry\":null},\"client\":{\"userAgent\":{\"rawUserAgent\":\"AHC/1.0\",\"os\":\"Unknown\",\"browser\":\"UNKNOWN\"},\"zone\":\"null\",\"device\":\"Unknown\",\"id\":null,\"ipAddress\":\"52.35.147.213\",\"geographicalContext\":{\"city\":\"Boardman\",\"state\":\"Oregon\",\"country\":\"United States\",\"postalCode\":\"97818\",\"geolocation\":{\"lat\":45.8491,\"lon\":-119.7143}}},\"authenticationContext\":{\"authenticationProvider\":null,\"credentialProvider\":null,\"credentialType\":null,\"issuer\":null,\"interface\":null,\"authenticationStep\":0,\"externalSessionId\":\"trszfCx9W0STJSVtdYvCe7adg\"},\"displayMessage\":\"Update user profile for Okta\",\"eventType\":\"user.account.update_profile\",\"outcome\":{\"result\":\"SUCCESS\",\"reason\":null},\"published\":\"2018-11-26T19:23:24.093Z\",\"securityContext\":{\"asNumber\":null,\"asOrg\":null,\"isp\":null,\"domain\":null,\"isProxy\":null},\"severity\":\"INFO\",\"debugContext\":{\"debugData\":{\"requestId\":\"W-xILP4inm2dx@t856BdaAAABrc\",\"requestUri\":\"/api/v1/users/n1.g@ewb11.com\",\"changedAttributes\":\"CIS_Number,lastName,firstName,mobilePhone\",\"url\":\"/api/v1/users/n1.g@ewb11.com?\"}},\"legacyEventType\":\"core.user.config.profile_update.success\",\"transaction\":{\"type\":\"WEB\",\"id\":\"W-xILP4inm2dx@t856BdaAAABrc\",\"detail\":{}},\"uuid\":\"f5f48b1b-9ba7-464b-ac87-8b046057ce3f\",\"version\":\"0\",\"request\":{\"ipChain\":[{\"ip\":\"52.35.147.213\",\"geographicalContext\":{\"city\":\"Boardman\",\"state\":\"Oregon\",\"country\":\"United States\",\"postalCode\":\"97818\",\"geolocation\":{\"lat\":45.8491,\"lon\":-119.7143}},\"version\":\"V4\",\"source\":null}]},\"target\":null,\"__^tokenizedvalues^__\":[]}"));
    }

}
