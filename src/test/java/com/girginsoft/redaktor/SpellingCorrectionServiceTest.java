/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.girginsoft.redaktor;

import com.girginsoft.sociolog.service.SpellingCorrectionServiceTr;
import java.util.*;
import static org.hamcrest.Matchers.equalTo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author girginsoft
 */
public class SpellingCorrectionServiceTest {

    @Rule
    public MyErrorCollector collector = new MyErrorCollector();
    private static int truePositive = 0;
    private static int trueNegative = 0;
    private static int falseNegative = 0;
    private static int falsePositive = 0;
    private static double overallScore = 0;
    private static SpellingCorrectionServiceTr service = null;
    private String clean(String word) {
        word = word.replaceAll("[^\\p{L}\\p{Nd}]+", "");
        return word;
    }
    public void assertSententences(String expected, String result, String org, HashMap<String, ArrayList<String>> allsug) {
        List<String> expectedToken = Arrays.asList(expected.split("\\s"));
        List<String> resultToken = Arrays.asList(result.split("\\s"));
        List<String> originalToken = Arrays.asList(org.split("\\s"));
        double score = 0;
        for (int i = 0; i < originalToken.size(); i++) {
            String exp = expectedToken.get(i) == null ? "" : expectedToken.get(i).toLowerCase(new Locale("tr", "TR"));
            String res = resultToken.get(i) == null ? "" : resultToken.get(i).toLowerCase(new Locale("tr", "TR"));
            String original = originalToken.get(i) == null ? "" : originalToken.get(i).toLowerCase(new Locale("tr", "TR"));
            exp = clean(exp);
            res = clean(res);
            original = clean(original);
            collector.checkThat(res, equalTo(exp));
            if (original.equals(exp)) { //positive
                if (exp.equals(res)) {
                    truePositive++;
                } else {
                    falseNegative++;
                }
            } else { //negative
                if (original.equals(res)) {
                    falsePositive++;
                } else {
                    trueNegative++;
                    ArrayList<String> suggestions = allsug.get(original);
                    if (suggestions != null && suggestions.size() > 0) {
                        if (clean(suggestions.get(0)).equals(exp)) {
                            score = score + 1;
                        } else {
                            for (String sug : suggestions) {
                                if (clean(sug).equals(exp)) {
                                    score = score + 0.5;
                                }
                            }
                        }
                    } 
                }
            }
            
           
            
        }
        overallScore += score;
        
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        service = SpellingCorrectionServiceTr.getInstance();
        service.learn();
    }

    @AfterClass
    public static void printResult() {
        System.out.println("----------------------------");
        System.out.println("|" + truePositive + "|" + falseNegative + "|");
        System.out.println("|" + falsePositive + "|" + trueNegative + "|");
        System.out.println("----------------------------");
        double accuracy = (double) (truePositive + trueNegative) / (double) (trueNegative + truePositive + falseNegative + falsePositive);
        System.out.println("Accuracy:% " + accuracy * 100);
        System.out.println("Total Corrected: " + falseNegative + " / " + (falseNegative + falsePositive));
        double sa = (double) overallScore / (double) trueNegative;
        System.out.println("SA: " + sa);
    }

    @Test
    public void tweet1() {
        String original = "#Turkcell kullanmasam da son reklamı çok güzel olmuş..her seferinde gözler doluyor :) Ahmet Mümtaz Taylan döktürmüş..";
        HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        String result = service.predict(original, suggestions);
        String expected = "#Turkcell kullanmasam da son reklamı çok güzel olmuş..her seferinde gözler doluyor :) Ahmet Mümtaz Taylan döktürmüş..";
        assertSententences(expected, result, original, suggestions);
    }

    @Test
    public void tweet2() {
        String original = "#Turkcell Son iki yilda Avrupa'nin en hizli buyuyen telekom grubu olmus. Turk Telekom da listede";
         HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        String result = service.predict(original, suggestions);
        String expected = "#Turkcell Son iki yılda Avrupa'nın en hızlı büyüyen telekom grubu olmuş. Türk Telekom da listede";
        assertSententences(expected, result, original, suggestions);
    }

    @Test
    public void tweet3() {
        String original = "Whatshapda neymis Trcell bip bi harika uygulamayi indirin 3 ay bedava 1gb internet hediyesini gule gule kullanin tesekkurler #turkcell";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        String result = service.predict(original, suggestions);
        String expected = "Whatshapda neymiş Trcell bip bi harika uygulamayı indirin 3 ay bedava 1gb internet hediyesini güle güle kullanın teşekkürler #turkcell";
        assertSententences(expected, result, original, suggestions);
    }

    @Test
    public void tweet4() {
        String original = "Taşındık interneti yavaş da olsa #turkcell üzerinden bağlarım pc'ye dedim ama ne yavaşı böyle upload ttnet'de bile yk";
        HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        String result = service.predict(original, suggestions);
        String expected = "Taşındık interneti yavaş da olsa #turkcell üzerinden bağlarım pc'ye dedim ama ne yavaşı böyle upload ttnet'de bile yok";
        assertSententences(expected, result, original, suggestions);
    }

    @Test
    public void tweet5() {
        String original = "Turkcell daha once soylemis miydim.. Cok iyisin, mukemmelsin #turkcell";
         HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        String result = service.predict(original, suggestions);
        String expected = "Turkcell daha önce söylemiş miydim.. Çok iyisin, mükemmelsin #turkcell";
        assertSententences(expected, result, original, suggestions);
    }

    @Test
    public void tweet6() {
        String original = "#turkcell platinyumu cok seviyorum her isimi hallesiyor  #turkcell i olmak bi ayricalikmis simdi anliyorum!";
         HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        String result = service.predict(original, suggestions);
        String expected = "#turkcell platinyumu çok seviyorum her işimi hallediyor  #turkcell i olmak bi ayrıcalıkmış şimdi anlıyorum!";
        assertSententences(expected, result, original, suggestions);
    }

    @Test
    public void tweet7() {
        String text = "Son gunlerin en guzel radyo spotu #hepsiburada.com. Cok begeniyorum";
        String expected = "Son günlerin en güzel radyo spotu #hepsiburada.com. Çok beğeniyorum";
        HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        String result = service.predict(text, suggestions);
        assertSententences(expected, result, text, suggestions);
    }

    @Test
    public void tweet8() {
        String text = "#turkcell pahalıda olsa vazgeçemememin sebebi bu. müşteri hizmetleri problemimi hemen çözdü. teşekkürler #turkcell :)";
        String expected = "#turkcell pahalıda olsa vazgeçemememin sebebi bu. müşteri hizmetleri problemimi hemen çözdü. teşekkürler #turkcell :)";
        HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet9() {
        String text = "Turkcell sevgim, #Turkcell'de calisiyorum diye degil, gercek su ki #Turkcell `i seviyorum diye, Turkcell`de calisiyorum";
        String expected = "Turkcell sevgim, #Turkcell'de çalışıyorum diye değil, gerçek şu ki #Turkcell `i seviyorum diye, Turkcell`de çalışıyorum";
         HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet10() {
        String text = "Arkadasim sevgilisinin adini telefonuna #turkcell diye kaydetmis evdekiler anlamasin diye, e zaten #turkcell de zirt pirt mesaj atiyor :)";
        String expected = "Arkadaşım sevgilisinin adını telefonuna #turkcell diye kaydetmiş evdekiler anlamasın diye, e zaten #turkcell de zırt pırt mesaj atıyor :)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet11() {
        String text = "#Turkcell tagı altında musterılerının sıkayetlerını dınleyıp yardımcı oluyor. Tesekkurler #Turkcell";
        String expected = "#Turkcell tagı altında müşterilerinin şikâyetlerini dinleyip yardımcı oluyor. Teşekkürler #Turkcell";
      HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        assertSententences(expected, service.predict(text, suggestions), text, suggestions);

    }

    @Test
    public void tweet12() {
        String text = "#turkcell asmali mescit'te sokak hayvanlari için su kaplari birakmis, tebrikler";
        String expected = "#turkcell asmalı mescit'te sokak hayvanları için su kapları bırakmış, tebrikler";
     HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet13() {
        String text = "Şunu okurken heycanlanmak için çok fazla futbol bilgisine ihtiyaç yok diye düşünüyorum. Hissediyor insan. #vodafone.";
        String expected = "Şunu okurken heycanlanmak için çok fazla futbol bilgisine ihtiyaç yok diye düşünüyorum. Hissediyor insan. #vodafone. ";
  HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet14() {
        String text = "#Avea Reklamı çok iyi olmuş :)";
        String expected = "#Avea Reklamı çok iyi olmuş :)";
   HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet15() {
        String text = "Sanırım Avea'nın fakirliğini seviyorum. Yoksa neden katlanayım, en güzel yerde çekmeyen operatöre? #avea.";
        String expected = "Sanırım Avea'nın fakirliğini seviyorum. Yoksa neden katlanayım, en güzel yerde çekmeyen operatöre? #avea.";
  HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();
        assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet16() {
        String text = "Yine bir bayram ve yine eski calisanini unutmayip cikolotasini gonderen @Avea.. Supersiniz, tesekkurler #Avea #bayram";
        String expected = "Yine bir bayram ve yine eski çalışanını unutmayıp çikolatasını gönderen @Avea.. Süpersiniz, teşekkürler #Avea #bayram";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>(); 
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet17() {
        String text = "Masaya eski model cep telefonu koyarsan ezik durumuna düşersin mesajlı harika reklamı yazan yaratıcı beyni kutluyorum #AvealılarBilir #AVEA";
        String expected = "Masaya eski model cep telefonu koyarsan ezik durumuna düşersin mesajlı harika reklamı yazan yaratıcı beyni kutluyorum #AvealılarBilir #AVEA";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();  
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet18() {
        String text = "Erken gelen 14 Subat hediyem :) Iyiki varsin #avea iyi ki varsin #woops :))";
        String expected = "erken gelen 14 şubat hediyem :) iyiki varsın #avea iyi ki varsın #woops :))";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();  
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet19() {
        String text = "Nasil olsa en çok mesaj atan benim sevgilin olmasada olur diyerekten beni sevdiğini söyleyen aveaya sevgilerle #avea.";
        String expected = "Nasıl olsa en çok mesaj atan benim sevgilin olmasada olur diyerekten beni sevdiğini söyleyen aveaya sevgilerle #avea.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet20() {
        String text = "Dogum gunum dolayisiyla tum gun gorusmelerimin ucretsiz olacagini ileten #avea cansin :) tesekkurler #avea";
        String expected = "doğum günüm dolayısıyla tüm gün görüşmelerimin ücretsiz olacağını ileten #avea cansın :) teşekkürler #avea";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet21() {
        String text = "yavrum aldım mesajını da turkcell değil misin ki sen? gitmiyo :/";
        String expected = "yavrum aldım mesajını da turkcell değil misin ki sen? gitmiyor :/";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>(); 
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet22() {
        String text = "#lcwaikiki deki o iğrenç elbiseye 162 tl veren var mıdır acaba? psikolojim bozuldu hiç iyi değilim";
        String expected = "#lcwaikiki deki o iğrenç elbiseye 162 tl veren var mıdır acaba? psikolojim bozuldu hiç iyi değilim";
           
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>(); 
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);

    }

    @Test
    public void tweet23() {
        String text = "#hepsiburada nin bu kadar kalitesiz hizmet verdigini bilmiyordum. Ilgi alaka ve musteri hizmetleri su anda 0 in altinda";
        String expected = "#hepsiburada nın bu kadar kalitesiz hizmet verdiğini bilmiyordum. İlgi alaka ve müşteri hizmetleri şu anda 0 in altında";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();  
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet24() {
        String text = "Stokta oldugunu iddia ettigi urunu 11 gundur gonderememekle kalmayip bir gonderim tarihi bile veremeyen #hepsiburada yi siddetle kiniyorum";
        String expected = "Stokta olduğunu iddia ettiği ürünü 11 gündür gönderememekle kalmayıp bir gönderim tarihi bile veremeyen #hepsiburada yı şiddetle kınıyorum";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>(); 
       
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet25() {
        String text = "Sipariş ettiğim 27 kutu yaş mamadan sadece 1'ini, minik bir kutu içinde kargoya vermişler, diğerleri paketleniyormuş. #hepsiburada salaklığı";
        String expected = "Sipariş ettiğim 27 kutu yaş mamadan sadece 1'ini, minik bir kutu içinde kargoya vermişler, diğerleri paketleniyormuş. #hepsiburada salaklığı";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);

    }

    @Test
    public void tweet26() {
        String text = "Bosuna yeni hat aldigim pek guzel olduu eski numaramla devam ediciiimm. Adisin, ise yaramaZsin #avea Yuru git la.";
        String expected = "Boşuna yeni hat aldığım pek güzel oldu eski numaramla devam edeceğim. Adisin, ise yaramazsın #avea Yürü git lan.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet27() {
        String text = "#avea aramayin artik iliskimi kesiyorum son faturayi merak etmeyin o fahis ucretiniz odedim. en kisa zamanda gecisimi yapiyorum !!.";
        String expected = "#avea aramayın artık ilişkimi kesiyorum son faturayı merak etmeyin o fahiş ücretiniz ödedim. en kısa zamanda geçişimi yapıyorum !!.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet28() {
        String text = "Kusadasinin bir bolumunde cekmeyen #avea nerde cekmeyi dusunuyorsun acaba. Bu kadar mi zayif bi sebeke oldunuz siz.";
        String expected = "Kuşadasının bir bölümünde çekmeyen #avea nerde çekmeyi düşünüyorsun acaba. Bu kadar mı zayıf bi şebeke oldunuz siz.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();  
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet29() {
        String text = "çekmeye çekmeye çekilmez hale geldin be #avea en kısa zamanda ayrılmak dileğiyle :)";
        String expected = "çekmeye çekmeye çekilmez hale geldin be #avea en kısa zamanda ayrılmak dileğiyle :)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();  
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet30() {
        String text = "Senin gibi hatti ben var ya sabahtan beri kestiginiz kontor haram zikkim olsun Allah evinize atesler salsin #avea allah bildigi gibi yapsin.";
        String expected = "Senin gibi hattı ben var ya sabahtan beri kestiğiniz kontör haram zıkkım olsun Allah evinize ateşler salsın #avea allah bildiği gibi yapsın.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet31() {
        String text = "Hayir internet sitesi uzerinden tarife degistirirken boyle problemler cikacaksa neden siteye boyle bir tool koydun #vodafone.";
        String expected = "Hayır internet sitesi üzerinden tarife değiştirirken böyle problemler çıkacaksa neden siteye böyle bir tool koydun #vodafone.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet32() {
        String text = "#vodafone Kullaniyorsan teras katta oturman gerekiyor... şebeke tuğla görmesin yeterki !!!";
        String expected = "#vodafone Kullanıyorsan teras katta oturman gerekiyor... şebeke tuğla görmesin yeterki !!!";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet33() {
        String text = "sevgili #turkcell benden habersiz internet paketi yapmanı paralarımı sövüşlemeni unutmicam. kader utansın kı etrafımdakiler #turkcell";
        String expected = "sevgili #turkcell benden habersiz internet paketi yapmanı paralarımı söğüşlemeni unutmayacağım. kader utansın ki etrafımdakiler #turkcell";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet34() {
        String text = "#turkcell anlasilan eski tarifeden orn. kampanyali iphone'a gectiginizde eski internet veya sms paketlerini iptal etmiyor! tongaya dusmeyin!.";
        String expected = "#turkcell anlaşılan eski tarifeden örn. kampanyalı iphone'a geçtiğinizde eski internet veya sms paketlerini iptal etmiyor! tongaya düşmeyin!.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet35() {
        String text = "#turkcell ve bayileri musterilerini nasil soyacaklarini sasirmis! siz siz olun #turkcell ve bayilerinden hizmet alirken 15817 kere dusunun!";
        String expected = "#turkcell ve bayileri müşterilerini nasıl soyacaklarını şaşırmış! siz siz olun #turkcell ve bayilerinden hizmet alırken 15817 kere düşünün!";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet36() {
        String text = "Musteri Hizmetleri ile gorusmeyi ucretli yapan Turkcell'i kiniyorum #turkcell";
        String expected = "Müşteri Hizmetleri ile görüşmeyi ücretli yapan Turkcell'i kınıyorum #turkcell";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet37() {
        String text = "Dert para kazanmak olunca ölüden de diriden de faydalanan #Turkcell bari bugün yapmayaydın...";
        String expected = "Dert para kazanmak olunca ölüden de diriden de faydalanan #Turkcell bari bugün yapmasaydın...";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet38() {
        String text = "Evimin arka odasinda Istanbul'un gobeginde 3Gsi cekmeyen #turkcell sen bu reklamlari yaparken neyin kafasini yasiyorsun?";
        String expected = "Evimin arka odasında İstanbul'un göbeğinde 3Gsi çekmeyen #turkcell sen bu reklamları yaparken neyin kafasını yaşıyorsun?";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet39() {
        String text = " 4 aydir benden uye olmadigim oyun sitelerinden para cekip 2 gunde 1 gb internet kullanimi yaptigimi iddia edip ispatlayamayan #turkcell";
        String expected = " 4 aydır benden üye olmadığım oyun sitelerinden para çekip 2 günde 1 gb internet kullanımı yaptığımı iddia edip ispatlayamayan #turkcell";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet40() {
        String text = " Acaba hangi kampanyadan yararlanabilirim #turkcell allah belanı versin #turkcell ";
        String expected = " Acaba hangi kampanyadan yararlanabilirim #turkcell allah belanı versin #turkcell ";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet41() {
        String text = "Daha iyi çeksin diye #turkcell den #vodafon ageçtim, kötü mü yaptım acaba? Olduğum yerde ikisi çekiyor o çekmiyor.";
        String expected = "Daha iyi çeksin diye #turkcell den #vodafonea geçtim, kötü mü yaptım acaba? Olduğum yerde ikisi çekiyor o çekmiyor. ";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);

    }

    @Test
    public void tweet42() {
        String text = "#turkcell son zamanlarda internette neden bu kadar yavaş";
        String expected = "#turkcell son zamanlarda internette neden bu kadar yavaş";

       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet43() {
        String text = "Mutluluk, gnctrkcll kampanyasını bilmeden iki kişi kahve dünyasına gidip, kahve içtikten sonra ikincisinin bedava olduğunu öğrenmekti bugün.";
        String expected = "Mutluluk, gnctrkcll kampanyasını bilmeden iki kişi kahve dünyasına gidip, kahve içtikten sonra ikincisinin bedava olduğunu öğrenmekti bugün.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();      
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet44() {
        String text = "Bu Turkcell ile aramız her ne kadar iyi olmasa da böyle hediyeler verdikce gözüme giriyorlar :D";
        String expected = "Bu Turkcell ile aramız her ne kadar iyi olmasa da böyle hediyeler verdikçe gözüme giriyorlar :D";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);

    }

    @Test
    public void tweet45() {
        String text = " Bu arada siz sevgili panpişlerime duyurmayi unuttum vodafone un ayricaliklarla dolu dunyasindan turkcell in indirimlerle dolu dunyasina";
        String expected = " Bu arada siz sevgili panpişlerime duyurmayı unuttum vodafone un ayrıcalıklarla dolu dünyasından turkcell in indirimlerle dolu dünyasına";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();  
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet46() {
        String text = "aciklamayi taze taze intrnette okuuyunca güldm.";
        String expected = "açıklamayı taze taze internette okuyunca güldüm.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet47() {
        String text = "superonline cok iyi hele ttnetten sonra baya iyi 3 aydir sorunsuz kullaniorm hic skntı cikarmadi";
        String expected = "superonline çok iyi hele ttnetten sonra baya iyi 3 aydır sorunsuz kullanıyorum hiç sıkıntı çıkarmadı";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet48() {
        String text = "Manga Konseri harikydı. Acun Ilıcalı'ya, Manga'ya ve Turkcell'e teşekkür ediyorum.";
        String expected = "Manga Konseri harikaydı. Acun Ilıcalı'ya, Manga'ya ve Turkcell'e teşekkür ediyorum.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }

    @Test
    public void tweet49() {
        String text = "Bir tim calisani  bana kahve ismarliyor :)) tesekkurler  =) ( Kahve Dünyası)";
        String expected = "Bir tim çalışanı  bana kahve ısmarlıyor :)) teşekkürler  =) ( Kahve Dünyası)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
     @Test
    public void tweet50() {
        String text = "#avea reklamcılıkta dokturuyo :) necefli maşrapa guzel fikirdi ;)";
        String expected = "#avea reklamcılıkta döktürüyor :) necefli maşrapa güzel fikirdi ;)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet51() {
        String text = "#Turkcell Akıllı Otomobil Platformu, ilk olarak Subaru ve Volvo marka otomobillerle sunuluyor";
        String expected = "#Turkcell Akıllı Otomobil Platformu, ilk olarak Subaru ve Volvo marka otomobillerle sunuluyor";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();       
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet52() {
        String text = "#Turkcell 20. yasini kutluyor, Süreyya Ciliv sahnede Turkcell'in gelisim hikayesini anlatiyor...";
        String expected = "#Turkcell 20. yaşını kutluyor, Süreyya Ciliv sahnede Turkcell'in gelişim hikâyesini anlatıyor...";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet53() {
        String text = "Dün arayan #turkcell mevcut operatorumun verdiği kadar dakikayı aynı fiyata teklf etti. ";
        String expected = "Dün arayan #turkcell mevcut operatörümün verdiği kadar dakikayı aynı fiyata teklif etti. ";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet54() {
        String text = "Neden geçeyim ki farkı nedir dedim. Cok kaliteliymis";
        String expected = "Neden geçeyim ki farkı nedir dedim. Çok kaliteliymiş";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet55() {
        String text = "3.8 milyon okuma yazma bilmeyen TC vatandasindan 3.1 milyonu kadin!!!!! #Turkcell ";
        String expected = "3.8 milyon okuma yazma bilmeyen TC vatandaşından 3.1 milyonu kadın!!!!! #Turkcell ";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet56() {
        String text = "harvard'li emily portakal suyu icerken NY times'da turkiye'deki kadin cinayetleriyle ilgili bir haber okurken 'iyyy horribil' dedi #turkcell";
        String expected = "harvard'lı emily portakal suyu içerken NY times'ta türkiye'deki kadın cinayetleriyle ilgili bir haber okurken 'iyyy horribil' dedi #turkcell";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet57() {
        String text = "20 Mayis'tan sonra telefonunuz kapaliyken ya da cekmiyorken sizi kimin aradigini öğrendiğiniz mesaj 50 kurus";
        String expected = "20 Mayıs'tan sonra telefonunuz kapalıyken ya da çekmiyorken sizi kimin aradığını öğrendiğiniz mesaj 50 kuruş";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet58() {
        String text = "sevgili #turkcell hayat paylaşinca güzelse, al bu benim cep faturam, yarı yarıya paylasalimda sende bu güzellikten payını al bari #retrotweet";
        String expected = "sevgili #turkcell hayat paylaşınca güzelse, al bu benim cep faturam, yarı yarıya paylaşalımda sende bu güzellikten payını al bari #retrotweet";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet59() {
        String text = "@Atiye gectigimiz gunlerde iddiali kiyafetiyle #Avea partisinde goz kamastirdi ! ";
        String expected = "@Atiye geçtiğimiz günlerde iddialı kıyafetiyle #Avea partisinde göz kamaştırdı ! ";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet60() {
        String text = "Mobil iletisim firmasi Avea logosunu yenilemis :) ama bir yerden tanidik geliyo sanki #avea ";
        String expected = "Mobil iletişim firması Avea logosunu yenilemiş :) ama bir yerden tanıdık geliyor sanki #avea ";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();  
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet61() {
        String text = "Bu nasıl anlamsiz bir mesajdir!! Kim olduğunu mesajda belirtseydin daha iyi olmaz miydi! @avea @aveadestek #avea #fail";
        String expected = "Bu nasıl anlamsız bir mesajdır!! Kim olduğunu mesajda belirtseydin daha iyi olmaz mıydı! @avea @aveadestek #avea #fail";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet62() {
        String text = "'Y Kusagini Anlamak' konferansi canli yayinda izlemek @evrimkuran #Avea yoneticilerine anlatiyor.. ";
        String expected = "'Y Kuşağını Anlamak' konferansı canlı yayında izlemek @evrimkuran #Avea yöneticilerine anlatıyor..";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet63() {
        String text = "#avea reklamcılıkta dokturuyo :) necefli maşrapa guzel fikirdi ;)";
        String expected = "#avea reklamcılıkta döktürüyor :) necefli maşrapa güzel fikirdi ;)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet64() {
        String text = "#avea reklamlarindaki fasulye faturadan dert yaniyor; ama bogazda cay iciyor, tuhaf geldi bana nedense.";
        String expected = "#avea reklamlarındaki fasülye faturadan dert yanıyor; ama boğazda cay içiyor, tuhaf geldi bana nedense.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
      @Test
    public void tweet65() {
        String text = "İste bu yuzden #avea: Sosyal sorumluluk calismalarinin reklamini yapmayi sirket olarak etik bulmuyoruz. Ali Yilmaz";
        String expected = "İşte bu yüzden #avea: Sosyal sorumluluk çalışmalarının reklamını yapmayı şirket olarak etik bulmuyoruz. Ali Yılmaz";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet66() {
        String text = "#avea reklamlarindaki aveasiz cocuk neden bilmem bana hep aveali cocuk algisini veriyor. Diger turlusune alisamadim";
        String expected = "#avea reklamlarındaki aveasız çocuk neden bilmem bana hep avealı çocuk algısını veriyor. Diğer türlüsüne alışamadım";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet67() {
        String text = "#hepsiburada japon kaynakli bir fona satilmak uzereymis, guncel bilgisi olan var mi? Mitsui galiba. @webrazzi";
        String expected = "#hepsiburada japon kaynaklı bir fona satılmak üzereymiş, güncel bilgisi olan var mı? Mitsui galiba. @webrazzi";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet68() {
        String text = "#LCWaikiki biliyorum ulkemize yaptigi katkiyi ve istihdami. Bu oyunlara inat ilk giyisi ihtiyacimi #LCWaikiki den karsilayacagim.";
        String expected = "#LCWaikiki biliyorum ülkemize yaptığı katkıyı ve istihdamı. Bu oyunlara inat ilk giysi ihtiyacımı #LCWaikiki den karşılayacağım.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();      
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet69() {
        String text = "iphonedan kullanilan online islemlerin icinde superonline bilgilerimizde olsa daha faydali olur.";
        String expected = "iphonedan kullanılan online işlemlerin içinde superonline bilgilerimizde olsa daha faydalı olur.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet70() {
        String text = "#vodafone icin Buyuk eksiklik. Apple urunlerinde kullanilmak uzere #turkcell online islem gibi bir app yapmadilar, yapamadilar.";
        String expected = "#vodafone için Büyük eksiklik. Apple ürünlerinde kullanılmak üzere #turkcell online işlem gibi bir app yapmadılar, yapamadılar.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
     @Test
    public void tweet71() {
        String text = "pişirmek istersen de bana sorarsın tcell e ihtiyacın yok gercekten";
        String expected =  "pişirmek istersen de bana sorarsın tcell e ihtiyacın yok gerçekten";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();  
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet72() {
        String text = "rehbere baktim olanlar portin yapmis turkcell kullanicisi olmuslar mavideki calisanlara sordum turkcell kullaniyorlar.";
        String expected = "rehbere baktım olanlar portin yapmış turkcell kullanıcısı olmuşlar mavideki çalışanlara sordum turkcell kullanıyorlar.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet73() {
        String text = "Sabah sabah teknoloji ozurlulugum tuttu turkcell time gittim tim kapali kopyalar telefonda hayatim film seridi gibi gecti korkudan.";
        String expected = "Sabah sabah teknoloji özürlülüğüm tuttu turkcell time gittim tim kapalı kopyalar telefonda hayatım film şeridi gibi geçti korkudan.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet74() {
        String text = "turkcell dergi isine nasladi bu arada e-dergi olarak. İdefix'te Geveze e-kitaplari satisi basladi...";
        String expected = "turkcell dergi işine başladı bu arada e-dergi olarak. İdefix'te Geveze e-kitapları satışı başladı...";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
      @Test
    public void tweet75() {
        String text = "abi birde turkcell'le röportaj yapsan cok iyi olur. Abi yine Skytürk tv'de yaptigin radyocu programi yapsan olmaz mi?";
        String expected = "abi birde turkcell'le röportaj yapsan çok iyi olur. Abi yine Skytürk tv'de yaptığın radyocu programı yapsan olmaz mı?";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet76() {
        String text = "Turkcell reklaminda oynayan sanatcilarla yolda karsilasirsam kulagindan tutup eve getirecegim";
        String expected = "Turkcell reklamında oynayan sanatçılarla yolda karşılaşırsam kulağından tutup eve getireceğim";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet77() {
        String text = "tam tünelden gecerken 3g cekiyordu :D türkcell'in reklamlari vodafone' harekete gecirmis olmali :)";
        String expected = "tam tünelden geçerken 3g çekiyordu :D turkcell'in reklamları vodafone' harekete geçirmiş olmalı :)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();      
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet78() {
        String text = "Yatak aldim, yorgan ve yastigi Turkcell verdi :)";
        String expected = "Yatak aldım, yorgan ve yastığı Turkcell verdi :)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet79() {
        String text = "Benım bedava sms hakkım var turkcell bana yenı yıl hedıyeesınıı erken yollamıss serefsızzz hahahah";
        String expected = "Benim bedava sms hakkım var turkcell bana yeni yıl hediyesini erken yollamış şerefsiz hahahah";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();      
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet80() {
        String text = "turkcell #ozturkmatik reklami allah belanizi vermesin!! Vapurda deli gibi guldurdun beni...";
        String expected = "turkcell #ozturkmatik reklamı allah belanızı vermesin!! Vapurda deli gibi güldürdün beni...";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
     @Test
    public void tweet81() {
        String text = "Beni bir tek dusunen turkcell ahey ahey";
        String expected = "Beni bir tek düşünen turkcell ahey ahey";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet82() {
        String text = "Sadece mutluluk vaadi veren reklamlardan baydim. Bana daha yaratici reklam mesajlariyla gel";
        String expected = "Sadece mutluluk vaadi veren reklamlardan baydım. Bana daha yaratıcı reklam mesajlarıyla gel";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();       
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet83() {
        String text = "aıle operatorumuz turkcell";
        String expected = "aile operatörümüz turkcell";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet84() {
        String text = "turkcell sun exspres arasindaki anlasmaya bi goz at hemen fiyatlar yari yariya ;)";
        String expected = "turkcell sun expres arasındaki anlaşmaya bi göz at hemen fiyatlar yarı yarıya ;)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
      @Test
    public void tweet85() {
        String text = "CocaCola ve Turkcell'in reklamcilarini alinlarindan opmek istiyorum";
        String expected = "CocaCola ve Turkcell'in reklamcılarını alınlarından öpmek istiyorum";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();      
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet86() {
        String text = "hizliyim ama hayirsiz degilimm :) bencede goruselim :) turkcell boy :)";
        String expected = "hızlıyım ama hayırsız değilim :) bencede görüşelim :) turkcell boy :)";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet87() {
        String text = "turkcell cvp atmis. Knka sitemzden bakabilirsiniz diye :p. Ve hala kitaptayim";
        String expected = "turkcell cevap atmış. Kanka sitemizden bakabilirsiniz diye :p. Ve hala kitaptayım";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();       
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet88() {
        String text = "allahtan gencim ve turkcell'liyim :) eglenmenize sevindim";
        String expected = "allahtan gencim ve turkcell'liyim :) eğlenmenize sevindim";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet89() {
        String text = "bu turkselde gecırıyoda geciriyor 29 fatura 50 gelıyor bi gecırmeyen sen kalmıstın türksel sende gecir okey";
        String expected = "bu turkcellde geçiriyorda geçiriyor 29 fatura 50 geliyor bi geçirmeyen sen kalmıştın turkcell sende geçir okey";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();       
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet90() {
        String text = "Verilen sure icinde belgelerinizi tamamlamadiginiz icin hattiniz aramalara kapatilmistir";
        String expected = "Verilen süre içinde belgelerinizi tamamlamadığınız için hattınız aramalara kapatılmıştır";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();      
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
     @Test
    public void tweet91() {
        String text = "korkunc bir ulkede yasiyoruz. herkes kazik atarak biseyler kazanma pesinde. #superonline da bunlarin basindaymis.";
        String expected = "korkunç bir ülkede yaşıyoruz. herkes kazık atarak bişeyler kazanma peşinde. #superonline da bunların başındaymış.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet92() {
        String text = "Cep telefonu melodısı turkcell sarkısı olan adam mehmet emın karamehmetın oglu herhalde.";
        String expected = "Cep telefonu melodisi turkcell şarkısı olan adam mehmet emin karamehmetin oğlu herhalde.";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet93() {
        String text = "nedir sizin tarife cozemezlerse 2 gunde 96 dan beri olan hattimi tasiycam artik";
        String expected = "nedir sizin tarife çözemezlerse 2 günde 96 dan beri olan hattımı taşıyacağım artık";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet94() {
        String text = "Kurumsalsaniz almak bu kadar zor mu?! Her seferinde sabrimizi zorlamasaniz... :(((";
        String expected = "Kurumsalsanız almak bu kadar zor mu?! Her seferinde sabrımızı zorlamasanız... :(((";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();   
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
      @Test
    public void tweet95() {
        String text = "dun vodafone catir cutur cekiyoodu, turkcell tirt! cekiyo cekmiyo reklamlarinin sonucu bu";
        String expected = "dün vodafone çatır çutur çekiyordu, turkcell tırt! çekiyor çekmiyor reklamlarının sonucu bu";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();       
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet96() {
        String text = "telefonum bozuk canim yani suan yolladigim mesaj ancak sabaha gelir sana turkcell bana pek ugrayamiyor da...";
        String expected = "telefonum bozuk canım yani şuan yolladığım mesaj ancak sabaha gelir sana turkcell bana pek uğrayamıyor da...";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet97() {
        String text = "Offf turkcell malsin kontur gondermek istiyorum ama sistem hatasi veriyo merveye gondermem lazim off offf acil yaa merveden cok ben istiyorm";
        String expected = "Off turkcell malsın kontör göndermek istiyorum ama sistem hatası veriyor merveye göndermem lazım off off acil yaa merveden çok ben istiyorum";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();    
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet98() {
        String text = "Turksel amca yapmiyo artik sana oyle kiyaaklar kankı";
        String expected = "Turkcell amca yapmıyor artık sana öyle kıyaklar kanka";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();      
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet99() {
        String text = "Bu gece saat 00.00'da mesaj atayımda sevgılısı attı sansın gerızekalı -Turkcell";
        String expected = "Bu gece saat 00.00'da mesaj atayımda sevgilisi attı sansın gerizekalı -Turkcell";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();     
       
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    @Test
    public void tweet100() {
        String text = "ısık hızında oldugunu ıddıa ettıklerı ınternetı 8 gunde baglayamıyorlar";
        String expected = "ışık hızında olduğunu iddia ettikleri interneti 8 günde bağlayamıyorlar";
       HashMap<String, ArrayList<String>> suggestions = new HashMap<String, ArrayList<String>>();      
       assertSententences(expected, service.predict(text, suggestions), text, suggestions);
    }
    
}
