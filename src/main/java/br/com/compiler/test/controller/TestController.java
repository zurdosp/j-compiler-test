package br.com.compiler.test.controller;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.mdkt.compiler.InMemoryJavaCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.compiler.test.model.ParamTestCode;
import br.com.compiler.test.model.ResponseTestCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/dev-code-test-platform/test/v1")
@Api(tags = "Teste code", description = "API for test code process")
public class TestController {

	private static final Logger logger = LoggerFactory.getLogger(TestController.class);

	private static final String argsMainParams = "args[";

	@ApiImplicitParams({ @ApiImplicitParam(name = "sourceMainToTest", value = "package br.com.compiler.test.controller;public class MainTest {public static void main(String[] args) {System.out.println(Integer.valueOf(args[0]) + Integer.valueOf(args[1])); System.out.println(args[0]);}}"), @ApiImplicitParam(name = "params", value = "\"1\", \"2\"")
	})
	@PostMapping("/execute")
	@ResponseBody
	public ResponseTestCode executeTest(@RequestBody ParamTestCode paramTestCode) throws Exception {
		logger.error("Trying to compile code: " + paramTestCode);
		String className = paramTestCode.getSourceMainToTest().substring(paramTestCode.getSourceMainToTest().indexOf("public class") + 13, paramTestCode.getSourceMainToTest().indexOf("{")).trim();
		File outputFileName = new File("outExecute.txt");
		FileOutputStream fos = new FileOutputStream(outputFileName);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		try {
			Class<?> testClass = InMemoryJavaCompiler.newInstance().compile("br.com.compiler.test.controller.".concat(className), paramTestCode.getSourceMainToTest());
			Method sumInstanceMethod = testClass.getMethod("main", String[].class);
			int argOccurrencesCount = StringUtils.countMatches(paramTestCode.getSourceMainToTest(), argsMainParams);
			String[] testeCodeParams = paramTestCode.getParams().get(0).split(",");

			if (testeCodeParams.length != argOccurrencesCount) {
				ResponseTestCode responseTestCode = new ResponseTestCode();
				responseTestCode.setCode(0);
				responseTestCode.setMessage("number of params tests mismatch");
				return responseTestCode;
			}

			String[] mainParams = new String[argOccurrencesCount];
			for (int i = 0; i < argOccurrencesCount; i++) {
				mainParams[i] = testeCodeParams[i].trim();
			}

			sumInstanceMethod.invoke(testClass, (Object) mainParams);
		} catch (Exception e) {
			Console con = System.console();
			System.out.println(con);
			logger.error("Erro ao tentar compilar codigo ", e);
			ResponseTestCode responseTestCode = new ResponseTestCode();
			responseTestCode.setCode(0);
			responseTestCode.setMessage(trataException(e));
			return responseTestCode;
		}
		ResponseTestCode responseTestCode = new ResponseTestCode();
		responseTestCode.setCode(1);
		String finalMsg = processFinalMessage(outputFileName).toString();
		responseTestCode.setMessage(finalMsg);
		return responseTestCode;
	}

	private List processFinalMessage(File outputFileName) {
		List<String> list = new ArrayList<>();

		try (Stream<String> stream = Files.lines(Paths.get(outputFileName.getName()))) {
			list = stream.collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;
	}

	private String trataException(Exception e) {

		if (e.getMessage().toString().split(",").length > 1) {
			return e.getMessage().toString().split(",")[2].toString().replace("message=", "").substring(0, e.getMessage().toString().split(",")[2].toString().replace("message=", "").length() - 1);
		}
		return e.getMessage().toString().split(",")[0].toString().replace("message=", "").substring(0, e.getMessage().toString().split(",")[0].toString().replace("message=", "").length() - 1);
	}

}
