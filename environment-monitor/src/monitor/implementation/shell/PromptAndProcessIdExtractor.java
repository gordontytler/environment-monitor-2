package monitor.implementation.shell;


public class PromptAndProcessIdExtractor {

	private int bashProcessId;
	private String prompt;

	public String extractPromptAndProcessId(String firstOutput, int lastNewline) {
		String withoutPrompt = firstOutput.substring(0, lastNewline);
		int newlineBeforeProcessId = withoutPrompt.lastIndexOf('\n');
		try {
			//bashProcessId = Integer.parseInt(withoutPrompt.substring(newlineBeforeProcessId + 1));
			bashProcessId = extractProcessId(withoutPrompt.substring(newlineBeforeProcessId + 1));
			//extractProcessId(firstOutput);
		} catch (NumberFormatException e) {
			return String.format("withoutPrompt: '%s'\nnewlineBeforeProcessId: %d\nsubstring: %s", withoutPrompt, newlineBeforeProcessId, withoutPrompt.substring(newlineBeforeProcessId + 1));
		}
		prompt = firstOutput.substring(lastNewline + 1);
		// TODO - this won't always work because 1) prompt can be anything 2) it may not have arrived yet 
		if (prompt.length() == 0) {
			String [] parts = firstOutput.split("echo $$\r\n");
			for (String part : parts) {
				if (part.lastIndexOf('@') != -1 || part.lastIndexOf('$') != -1 || part.lastIndexOf(' ') != -1) {
					if (part.length() > 1 && part.length() == part.trim().length() + 1  && part.lastIndexOf('\n') == -1 && part.lastIndexOf('\r') == -1) {
						prompt = part;
						return "OK";						
					}
					
				}
			}
			return "parts: " + String.format("%s", (Object[])parts); // for the exception
		}
		return "OK";
	}

	private int extractProcessId(String substring) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < substring.length(); i++) {
			char c = substring.charAt(i); 
			if (c >= '0' && c <= '9') {
				sb.append(c);
			}
		}
		return Integer.parseInt(sb.toString());
	}

	
	
	
	public int getBashProcessId() {
		return bashProcessId;
	}

	public String getPrompt() {
		return prompt;
	}
	
}
