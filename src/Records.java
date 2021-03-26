class Records {
    String[] splitData;
    String[] finalData;

    Records(){
        splitData = new String[0];
        finalData = new String[0];
    }
    Records(String dataLine){
        splitData = dataLine.split(",");
        finalData = quoteParse(splitData);
    }

/**
 * used to adjust string array for 'comma inside quote' edge case
 * <p>
 *     first counts no of extra tokens split due comma inside quotes
 *     then creates an array of adjusted size
 *     and stores it with comma separated tokens
 *     but in case of a token starting with '"' joins it together until the end quote
 *     and stores the final string as new token
 * </p>
 * @param  data string array with ',' separated tokens even inside quotes
 * @return finalData string array with ',' separated tokens but not inside quotes
 */
    private String[] quoteParse(String[] data){
        /* string to join together all tokens inside quote */
        String insideQuote = "";
        /* counter to store no of extra tokens */
        int cnt = 0;
        /* counter to iterate the finalData string */
        int cnt2 = 0;
        /* loop to count number of extra tokens */
        for (int i = 0; i<data.length; i++){
            if (data[i]!=""){
                if (data[i].charAt(0)=='\"') {
                    for (int j = i + 1; j < data.length; j++) {
                        if (data[j].charAt(data[j].length() - 1) == '\"') {
                            cnt ++;
                            break;
                        } else {
                            cnt++;
                        }
                    }
                }
            }
        }

        String[] finalData = new String[data.length-cnt];

        /* loop to store finalData with joined tokens inside quote */
        for (int i = 0; i<data.length; i++){
            if (data[i]!=""){
                if (data[i].charAt(0)=='\"') {
                    insideQuote = data[i].substring(1);
                    for (int j = i + 1; j < data.length; j++) {
                        if (data[j].charAt(data[j].length() - 1) == '\"') {
                            insideQuote = insideQuote + "," + data[j].substring(0, data[j].length() - 1);
                            i = j;
                            break;
                        } else {
                            insideQuote = insideQuote + "," + data[j];
                        }
                    }
                    finalData[cnt2] = insideQuote;
                }
                else{
                    finalData[cnt2] = data[i];
                }
            }
            cnt2++;
        }
        return finalData;
    }

}
