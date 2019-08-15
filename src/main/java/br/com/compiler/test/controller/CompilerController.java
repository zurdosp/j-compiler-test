package br.com.compiler.test.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import org.mdkt.compiler.InMemoryJavaCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.compiler.test.model.ResponseTestCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/dev-code-test-platform/compiler/v1")
@Api(tags = "Compiler Test", description = "API the compiler test of J")
public class CompilerController {

	private static final Logger logger = LoggerFactory.getLogger(CompilerController.class);

	/**
	 * Capture, compile and send do client ok or error message. this method only compile code.
	 * @param sourceCode
	 * @return
	 * 
	 * @throws Exception
	 */
	@ApiImplicitParams({ @ApiImplicitParam(name = "value", value = "package br.com.compiler.test.controller;public class MainTest {public static void main(String[] args) {}}")
	})
	@PostMapping("/execute")
	@ResponseBody
	public ResponseTestCode compilerTest(@RequestBody String sourceCode) throws Exception {
		logger.error("Trying to compile code: " + sourceCode);
		File outputFileName = new File("outCompile.txt");
		ResponseTestCode responseTestCode = new ResponseTestCode();
		FileOutputStream fos = new FileOutputStream(outputFileName);
		PrintStream ps = new PrintStream(fos);
		System.setOut(ps);
		try {
			Class<?> testClass = InMemoryJavaCompiler.newInstance().compile("br.com.compiler.test.controller.MainTest", sourceCode);
			Method sumInstanceMethod = testClass.getMethod("main", String[].class);
			if (testClass == null || sumInstanceMethod == null) {
				responseTestCode.setMessage("NOK");
				responseTestCode.setCode(0);
				logger.error("Compile code unsuccessfully");
			}
		} catch (Exception e) {
			logger.error("Error trying to compile code ", e);
			if (e.getMessage().toString().split(",").length >= 5) {
				responseTestCode.setMessage(e.getMessage().toString());
				return responseTestCode;
			}
			responseTestCode.setMessage(e.getMessage().toString().split(",")[2].toString() + e.getMessage().toString().split(",")[1].toString());
			return responseTestCode;
		}
		responseTestCode.setMessage("OK");
		responseTestCode.setCode(1);
		logger.debug("Compile code successfully");
		return responseTestCode;
	}

}
