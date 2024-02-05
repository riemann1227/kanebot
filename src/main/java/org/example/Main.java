package org.example;

import com.sun.tools.jconsole.JConsoleContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;


import java.awt.*;
import java.io.FileReader;
import java.sql.*;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Main extends ListenerAdapter{

    private static final String[] stocksList = {"삼성전자", "동경미오림시계", "테슬라", "올인시계", "APPLE", "SK하이닉스"};
    private static long[] stockChangeList = {0, 0, 0, 0, 0, 0};
    private static final String[] propertyList = {"시그니엘", "진우빌라", "반포자이", "충주2차푸르지오", "충주남산"};

    private static long[] propertyChangeList = {0, 0, 0, 0, 0};
    public static int timeMoved = 0;

    public static long givePropertyConst = 100000;
    public static final int seconds = 60;

    public static void main(String[] args) throws Exception{



        System.out.println(getStockData("삼성전자"));
        final String TOKEN = "";
        System.out.println("Kanebot Started!");
        JDA jda = JDABuilder.createDefault(TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

        jda.addEventListener(new Main());

        Timer schedular = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                timeMoved++;

                if(timeMoved % seconds == 0) {
                    changeStockRandomPrice();
                    changePropertyRandomPrice();
                    givePropertyMoney();
                    timeMoved = 0;

                }
            }
        };
        schedular.scheduleAtFixedRate(task, 0, 1000);
    }

    public static void stockDelist(String name){
        JSONObject userStocks = ReadData("user_stocks");

        Iterator i = userStocks.keySet().iterator();
        while(i.hasNext())
        {
            String userName = i.next().toString();
            JSONObject userStock = (JSONObject) userStocks.get(userName);
            if(userStock.containsKey(name)){
                ((JSONObject) userStocks.get(userName)).remove(name);
            }

        }
        WriteData(userStocks, "user_stocks");
    }

    public static void propertyDelist(String name){
        JSONObject userProperty = ReadData("user_property");

        Iterator i = userProperty.keySet().iterator();
        while(i.hasNext())
        {
            String userName = i.next().toString();
            JSONObject userOneProperty = (JSONObject) userProperty.get(userName);
            if(userOneProperty.containsKey(name)){
                ((JSONObject) userProperty.get(userName)).remove(name);
            }

        }
        WriteData(userProperty, "user_property");
    }

    public static void givePropertyMoney(){
        JSONObject userProperty = ReadData("user_property");

        Iterator i = userProperty.keySet().iterator();
        while(i.hasNext())
        {
            String userName = i.next().toString();
            JSONObject userOneProperty = (JSONObject) userProperty.get(userName);
            Iterator j = userOneProperty.keySet().iterator();
            while(j.hasNext()){
                String property = j.next().toString();
                long count =(long) ( ((JSONObject) userProperty.get(userName)).get(property));
                long cost = getPropertyData(property);
                changeBalance(userName, getAccountBalance(userName) + ((cost * count) / givePropertyConst));
            }

        }
    }
    public static void changeStockRandomPrice(){
        JSONObject obj = new JSONObject();
        for(int i=0;i<stocksList.length;i++){
            String name = stocksList[i];
            long price = getStockData(name);
            long change = (long) (Math.random() * 1000) - 500;
            price += change;
            stockChangeList[i] = change;
            if(price < 0){
                price = 0;
            }
            obj.put(name, price);
            if(price == 0){
                stockDelist(name);
            }
        }
        WriteData(obj, "stock");
    }

    public static void changePropertyRandomPrice(){
        JSONObject obj = new JSONObject();
        for(int i=0;i<propertyList.length;i++){
            String name = propertyList[i];
            long price = getPropertyData(name);
            long change = (long) (Math.random() * 1000000) - 500000;
            propertyChangeList[i] = change;
            price += change;
            if(price < 0){
                price = 0;
            }
            obj.put(name, price);
            if(price == 0){
                propertyDelist(name);
            }
        }
        WriteData(obj, "property");
    }
    public static long getStockData(String name){
        JSONObject obj = ReadData("stock");
        if(obj.containsKey(name)){
            return (Long) obj.get(name);
        }
        else{
            return -1;
        }
    }

    public static long getPropertyData(String name){
        JSONObject obj = ReadData("property");
        if(obj.containsKey(name)){
            return (Long) obj.get(name);
        }
        else{
            return -1;
        }
    }
    public static long getAccountBalance(String uid){
        JSONObject obj = ReadData("account");
        if(obj.containsKey(uid)){
            return (Long) obj.get(uid);
        }
        else{
            return -1;
        }
    }

    public static void makeAccount(String uid){
        JSONObject obj = ReadData("account");
        obj.put(uid, 100000);
        WriteData(obj, "account");
    }

    public static void changeBalance(String uid, long cost){
        JSONObject obj = ReadData("account");
        obj.put(uid, cost);
        WriteData(obj, "account");
    }

    public static int sendMonetToAccount(String uid1, String uid2, long cost){
        JSONObject obj = ReadData("account");
        if(obj.containsKey(uid1) && obj.containsKey(uid2)){
            long from = (long) obj.get(uid1);
            long to = (long) obj.get(uid2);
            if(cost > from){
                return -2;
            }
            if(cost < 0){
                return -3;
            }
            else{
                from -= cost;
                to += cost;
                obj.put(uid1, from);
                obj.put(uid2, to);
                WriteData(obj, "account");
                return 0;
            }

        }
        else{
            return -1;
        }
    }

    public static JSONObject getUserStocks(String uid){
        JSONObject obj = ReadData("user_stocks");
        if(obj.containsKey(uid)){
            return (JSONObject) obj.get(uid);
        }
        else{
            return new JSONObject();
        }
    }

    public static void changeUserStocks(String uid, String name, long count){
        JSONObject obj = ReadData("user_stocks");
        if(obj.containsKey(uid)){
            JSONObject user_stock = (JSONObject) obj.get(uid);
            user_stock.put(name, count);
            obj.put(uid, user_stock);
            WriteData(obj, "user_stocks");
        }
        else{
            JSONObject user_stock = new JSONObject();
            user_stock.put(name, count);
            obj.put(uid, user_stock);
            WriteData(obj, "user_stocks");
        }
    }

    public static long getOneStock(String uid, String stock){
        JSONObject obj = getUserStocks(uid);
        if(obj.containsKey(stock)){
            return (long) obj.get(stock);
        }
        else{
            return 0;
        }
    }

    public static JSONObject getUserProperty(String uid){
        JSONObject obj = ReadData("user_property");
        if(obj.containsKey(uid)){
            return (JSONObject) obj.get(uid);
        }
        else{
            return new JSONObject();
        }
    }

    public static void changeUserProperty(String uid, String name, long count){
        JSONObject obj = ReadData("user_property");
        if(obj.containsKey(uid)){
            JSONObject user_stock = (JSONObject) obj.get(uid);
            user_stock.put(name, count);
            obj.put(uid, user_stock);
            WriteData(obj, "user_property");
        }
        else{
            JSONObject user_stock = new JSONObject();
            user_stock.put(name, count);
            obj.put(uid, user_stock);
            WriteData(obj, "user_property");
        }
    }

    public static long getOneProperty(String uid, String name){
        JSONObject obj = getUserProperty(uid);
        if(obj.containsKey(name)){
            return (long) obj.get(name);
        }
        else{
            return 0;
        }
    }
    public static void WriteData(JSONObject obj, String name){
        try(FileWriter file = new FileWriter(String.format("./%s.json", name))){
            file.write(obj.toJSONString());
            file.flush();
            file.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static JSONObject ReadData(String name){
        JSONParser parser = new JSONParser();
        try(FileReader file = new FileReader(String.format("./%s.json", name))){
            JSONObject obj = (JSONObject) parser.parse(file);
            return obj;
        } catch (IOException e){
            e.printStackTrace();
        } catch (ParseException e){
            e.printStackTrace();
        }
        JSONObject obj = new JSONObject();
        return obj;
    }
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        String[] parsed = msg.split(" ");
        String cmd = parsed[0];
        if(cmd.equals("!도움말")){
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("케인봇 도움말");
            embed.setDescription("케인봇의 사용법 를! 알려주겠다 맨이야");
            embed.addField("!주식", "현재 주식 현황을 보여준다맨이야", false);
            embed.addField("!내주식", "현재 내 주식 현황을 보여준다맨이야", false);
            embed.addField("!거래 종목명 거래량", "주식을 거래 할 수 있다맨이야\n거래량에 풀매수 또는 풀매도를 입력하면 전체 거래를 진행 할 수 있다맨이야", false);
            embed.addField("!내계좌", "내 계좌 현황을 알려준다 맨이야", false);
            embed.addField("!계좌개설", "계좌 를! 개설 할 수 있다맨이야", false);
            embed.addField("!주식시간", "다음 변동까지 남은 시간 를! 알려준다 맨이야", false);
            embed.addField("!송금 금액", "사용자에게 송금 를! 할 수 있다 맨이야", false);
            embed.addField("!부동산", "현재 부동산 현황을 보여준다맨이야", false);
            embed.addField("!내부동산", "현재 내 부동산 현황을 보여준다맨이야", false);
            embed.addField("!부동산거래 부동산명 거래량", "부동산을 거래 할 수 있다맨이야\n거래량에 풀매수 또는 풀매도를 입력하면 전체 거래를 진행 할 수 있다맨이야", false);
            embed.setColor(0x42b580);
            event.getMessage().reply("").setEmbeds(embed.build()).queue();
        }
        if(cmd.equals("!게이는")){
            event.getMessage().reply("문화다").queue();
        }
        if(cmd.equals("!케인봇")){
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("케인봇 정보");
            embed.setDescription("자바로 새롭게 태어난 메카케인 이올시다.");
            embed.addField("버전", "JAVA-1.0.0", false);
            embed.addField("!도움말", "!도움말 로 케인봇 사용법을 알아보렴!", false);
            embed.setColor(0x42b580);
            event.getMessage().reply("").setEmbeds(embed.build()).queue();
            event.getMessage().reply("https://tenor.com/view/%EC%BC%80%EC%9D%B8-%EC%BC%80%EC%9D%B8tv-kane-%EC%82%AC%EC%BF%A0%EB%9E%80%EB%B3%B4-%EB%A1%9D%EB%A7%A8-gif-22225667").queue();
        }
        if(cmd.equals("!성준이는")){
            event.getMessage().reply("게이다").queue();
        }
        if(cmd.equals("!안녕하살법")){
            event.getMessage().reply("https://tenor.com/view/chika-fujiwara-gif-22892834").queue();
        }
        if(cmd.equals("!받아치기")){
            event.getMessage().reply("https://tenor.com/view/%EC%95%88%EB%85%95%ED%95%98%EC%82%B4%EB%B2%95%EB%B0%9B%EC%95%84%EC%B9%98%EA%B8%B0-%EC%8B%9C%EB%A1%9C%EA%B0%80%EB%84%A4%EC%BC%80%EC%9D%B4-shiroganekei-gif-20927935").queue();
        }
        if(cmd.equals("!최강한화")){
            event.getMessage().reply("https://tenor.com/view/%EC%BC%80%EC%9D%B8-%EA%B2%8C%EC%9D%B4%EC%A1%B0%EC%9D%B4%EA%B3%A0-%ED%95%9C%ED%99%94-%EA%B9%80%EC%84%B1%EA%B7%BC-%ED%83%80%EC%A7%80%EB%A6%AC-gif-21999460").queue();
        }
        if(cmd.equals("!호날두")){
            event.getMessage().reply("siuuuuuu!!!").queue();
        }
        if(cmd.equals("!부동산")){
            if(parsed.length < 2){
                DecimalFormat decFormat = new DecimalFormat("###,###");
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("전체 부동산 정보");
                embed.addField("부동산 갱신까지 남은 시간", String.format("%d초",  (seconds - (timeMoved % seconds)) ), false);
                for(int i=0;i<propertyList.length;i++){
                    String name = propertyList[i];
                    long cost = getPropertyData(name);
                    if(cost == 0){
                        embed.addField(name, "파산", false);
                    }
                    else{
                        if(propertyChangeList[i] >= 0){
                            embed.addField(name, String.format("%s₩", decFormat.format(cost))+ "\n" +  "▲" +  decFormat.format(propertyChangeList[i]) + "₩", false);
                        }
                        else{
                            embed.addField(name, String.format("%s₩", decFormat.format(cost))+ "\n" +  "▼" +  decFormat.format(propertyChangeList[i]) + "₩", false);
                        }
                    }
                }
                event.getMessage().reply("").setEmbeds(embed.build()).queue();
            }
            else {
                String property = parsed[1];
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(String.format("\"%s\"의 부동산 정보", property));
                embed.addField("현재 거래가", String.format("%d₩", getPropertyData(property)), false);
                event.getMessage().reply("").setEmbeds(embed.build()).queue();

            }
        }
        if(cmd.equals("!주식")){
            if(parsed.length < 2){
                DecimalFormat decFormat = new DecimalFormat("###,###");
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("전체 주식 정보");
                embed.addField("주가 갱신까지 남은 시간", String.format("%d초",  (seconds - (timeMoved % seconds)) ), false);
                for(int i=0;i<stocksList.length;i++){
                    String name = stocksList[i];
                    long cost = getStockData(name);
                    if(cost == 0){
                        embed.addField(name, "상장폐지", false);
                    }
                    else{
                        if(stockChangeList[i] >= 0){
                            embed.addField(name, String.format("%s₩", decFormat.format(getStockData(name)))+ "\n" +  "▲" +  decFormat.format(stockChangeList[i]) + "₩", false);
                        }
                        else{
                            embed.addField(name, String.format("%s₩", decFormat.format(getStockData(name)))+ "\n" +  "▼" +  decFormat.format(stockChangeList[i]) + "₩", false);
                        }
                    }
                }
                event.getMessage().reply("").setEmbeds(embed.build()).queue();
            }
            else{
                String stock = parsed[1];
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(String.format("\"%s\"의 주식 정보", stock));
                embed.addField("현재 주가", String.format("%d₩", getStockData(stock)), false);
                event.getMessage().reply("").setEmbeds(embed.build()).queue();

            }
        }
        if(cmd.equals("!내주식")){
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(event.getAuthor().getName() + " 님의 보유주식 정보");
            embed.addField("주가 갱신까지 남은 시간", String.format("%d초",  (seconds - (timeMoved % seconds)) ), false);

            JSONObject stocks = getUserStocks(event.getAuthor().getId());

            Iterator i = stocks.keySet().iterator(); // key값들을 모두 얻어옴.
            DecimalFormat decFormat = new DecimalFormat("###,###");
            long total = 0;
            while(i.hasNext())
            {
                String name = i.next().toString();
                long cost = getStockData(name);
                if(cost == 0){
                    long count = (long) stocks.get(name);
                    embed.addField(String.format("%s %d주", name, count), "상장폐지", false);
                }
                else{
                    long count = (long) stocks.get(name);
                    embed.addField(String.format("%s %d주", name, count), String.format("%s₩", decFormat.format(count * cost)), false);
                    total += count * cost;
                }

            }
            embed.addField("총 보유가", String.format("%s₩", decFormat.format(total)), false);
            event.getMessage().reply("").setEmbeds(embed.build()).queue();
        }
        if(cmd.equals("!내부동산")){
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(event.getAuthor().getName() + " 님의 보유 부동산 정보");
            embed.addField("부동산 갱신까지 남은 시간", String.format("%d초",  (seconds - (timeMoved % seconds)) ), false);

            JSONObject property = getUserProperty(event.getAuthor().getId());

            Iterator i = property.keySet().iterator(); // key값들을 모두 얻어옴.
            DecimalFormat decFormat = new DecimalFormat("###,###");
            long total = 0;
            while(i.hasNext())
            {
                String name = i.next().toString();
                long cost = getPropertyData(name);
                if(cost == 0){
                    long count = (long) property.get(name);
                    embed.addField(String.format("%s %d채", name, count), "파산", false);
                }
                else{
                    long count = (long) property.get(name);
                    embed.addField(String.format("%s %d채", name, count), String.format("%s₩", decFormat.format(count * cost)), false);
                    total += cost * count;
                }

            }
            embed.addField("총 보유가", String.format("%s₩", decFormat.format(total)), false);
            embed.addField("월 수령액", String.format("%s₩", decFormat.format(total / givePropertyConst)), false);
            event.getMessage().reply("").setEmbeds(embed.build()).queue();
        }
        if(cmd.equals("!거래")){
            if(parsed.length < 3){
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("거래할 종목과 수량을 입력해 주세요");
                event.getMessage().reply("").setEmbeds(embed.build()).queue();
            }
            else{
                String stock = parsed[1];
                long cost = getStockData(stock);
                long count = 0;
                if(parsed[2].equals("풀매수")){
                    long money = getAccountBalance(event.getMessage().getAuthor().getId());
                    count = money / cost;
                }
                else if(parsed[2].equals("반매수")){
                    long money = getAccountBalance(event.getMessage().getAuthor().getId());
                    count = (money / cost) / 2;
                }
                else if(parsed[2].equals("풀매도")){
                    count = getOneStock(event.getMessage().getAuthor().getId(), stock);
                }
                else if(parsed[2].equals("반매도")){
                    count = getOneStock(event.getMessage().getAuthor().getId(), stock);
                    count = count / 2;
                }
                else{
                    count = Long.parseLong(parsed[2]);
                }
                EmbedBuilder embed = new EmbedBuilder();
                if(cost == -1){
                    embed.setTitle("케인증권 주식 거래");
                    embed.addField("거래종목", "거래종목이 존재하지 않습니다", false);
                    event.getMessage().reply("").setEmbeds(embed.build()).queue();
                }
                else {
                    DecimalFormat decFormat = new DecimalFormat("###,###");
                    embed.setTitle("케인증권 주식 거래");
                    embed.addField("주가 갱신까지 남은 시간", String.format("%d초",  (seconds - (timeMoved % seconds)) ), false);
                    embed.addField("거래종목", stock, false);
                    embed.addField("현재 주가", String.format("%d₩", cost), false);
                    embed.addField("거래량", String.format("%s주",decFormat.format(count)), false);
                    embed.addField("거래액", String.format("%s₩", decFormat.format(cost * count)), false);
                    Button buttonBuy = Button.success("buyStock_"+stock+"_"+Long.toString(count), "매수");
                    Button buttonSell = Button.danger("sellStock_"+stock+"_"+Long.toString(count), "매도");
                    event.getMessage().reply("").setEmbeds(embed.build()).setActionRow(buttonBuy, buttonSell).queue();
                }
            }
        }

        if(cmd.equals("!부동산거래")){
            if(parsed.length < 3){
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("거래할 부동산과 수량을 입력해 주세요");
                event.getMessage().reply("").setEmbeds(embed.build()).queue();
            }
            else{
                String property = parsed[1];
                long cost = getPropertyData(property);
                long count = 0;
                if(parsed[2].equals("풀매수")){
                    long money = getAccountBalance(event.getMessage().getAuthor().getId());
                    count = money / cost;
                }
                else if(parsed[2].equals("풀매도")){
                    count = getOneProperty(event.getMessage().getAuthor().getId(), property);
                }
                else{
                    count = Long.parseLong(parsed[2]);
                }
                EmbedBuilder embed = new EmbedBuilder();
                if(cost == -1){
                    embed.setTitle("케인증권 부동산 거래");
                    embed.addField("거래", "거래종목이 존재하지 않습니다", false);
                    event.getMessage().reply("").setEmbeds(embed.build()).queue();
                }
                else {
                    DecimalFormat decFormat = new DecimalFormat("###,###");
                    embed.setTitle("케인증권 부동산 거래");
                    embed.addField("부동산 갱신까지 남은 시간", String.format("%d초",  (seconds - (timeMoved % seconds)) ), false);
                    embed.addField("거래종류", property, false);
                    embed.addField("현재 거래가", String.format("%s₩", decFormat.format(cost)), false);
                    embed.addField("거래량", String.format("%s채",decFormat.format(count)), false);
                    embed.addField("거래액", String.format("%s₩", decFormat.format(cost * count)), false);
                    embed.addField("월 수령액", String.format("%s₩", decFormat.format(cost * count / givePropertyConst)), false);
                    Button buttonBuy = Button.success("buyProperty_"+property+"_"+Long.toString(count), "구매");
                    Button buttonSell = Button.danger("sellProperty_"+property+"_"+Long.toString(count), "판매");
                    event.getMessage().reply("").setEmbeds(embed.build()).setActionRow(buttonBuy, buttonSell).queue();
                }
            }
        }

        if(cmd.equals("!주식시간")){
            event.getMessage().reply(String.format("주식 갱신까지 남은 시간은 %d 초 입니다!", (seconds - (timeMoved % seconds)))).queue();
        }
        if(cmd.equals("!계좌개설")){
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("케인증권 증권계좌 개설");
            embed.addField("계좌정보", "자유입출금계좌(이율: 0%)", false);
            embed.addField("주의사항", "1.계좌의 중복 개설은 불가합니다\n2.계좌 개설지 지원금 100000₩을 드립니다.", false);
            Button buttonAccount = Button.success("makeNormalAccount", "계좌개설");
            event.getMessage().reply("").setEmbeds(embed.build()).setActionRow(buttonAccount).queue();
        }
        if(cmd.equals("!내계좌")){
            if(getAccountBalance(event.getMessage().getAuthor().getId()) == -1){
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(event.getMessage().getAuthor().getName() + " 의 계좌 잔액");
                embed.addField("자유입출금계좌", "계좌가 없습니다!", false);
                event.getMessage().reply("").setEmbeds(embed.build()).queue();
            }
            else {
                DecimalFormat decFormat = new DecimalFormat("###,###");
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(event.getMessage().getAuthor().getName() + " 의 계좌 잔액");
                embed.addField("자유입출금계좌", String.format("%s₩", decFormat.format(getAccountBalance(event.getMessage().getAuthor().getId()))), false);
                event.getMessage().reply("").setEmbeds(embed.build()).queue();
            }
        }
        if(cmd.equals("!송금")){
            if(parsed.length < 2){
                event.getMessage().reply("송금할 금액을 입력하세요!").queue();
            }
            else {
                event.getMessage().reply("송금 받을 사람을 선택하세요(선택 후 변경은 불가능합니다)").addActionRow(
                        EntitySelectMenu.create("sendMoney_"+parsed[1], EntitySelectMenu.SelectTarget.USER)
                                .build()).queue();
            }
        }
    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event){
        String[] parsed = event.getComponentId().split("_");
        String cmd = parsed[0];
        if(cmd.equals("buyStock")){
            String stock = parsed[1];
            long count = Long.parseLong(parsed[2]);
            long cost = getStockData(stock);
            if(cost == 0){
                event.reply("상장폐지된 종목은 매수 할 수 없습니다!").queue();
            }
            else {
                System.out.println(event.getUser().getAvatarId());
                System.out.println(event.getUser().getName());
                String uid = event.getUser().getId();
                long balance = getAccountBalance(uid);
                if(balance < count * cost){
                    event.reply("계좌의 잔액이 부족합니다!").queue();
                }
                else{
                    changeBalance(uid, balance - (count * cost));
                    changeUserStocks(uid, stock, getOneStock(uid, stock) + count);
                    event.reply(event.getUser().getName() + " 의 거래 체결 완료!\n매수/" + stock + "/" + parsed[2] + "주").queue();
                    event.getMessage().delete().queue();
                }
            }
        }
        if(cmd.equals("sellStock")){
            String stock = parsed[1];
            long count = Long.parseLong(parsed[2]);
            long cost = getStockData(stock);
            System.out.println(event.getUser().getAvatarId());
            System.out.println(event.getUser().getName());
            String uid = event.getUser().getId();
            long balance = getAccountBalance(uid);
            if(count > getOneStock(uid, stock)){
                event.reply("보유 주식이 부족합니다!").queue();
            }
            else{
                changeBalance(uid, balance + (count * cost));
                changeUserStocks(uid, stock, getOneStock(uid, stock) - count);
                event.reply(event.getUser().getName() + " 의 거래 체결 완료!\n매도/" + stock + "/" + parsed[2] + "주").queue();
                event.getMessage().delete().queue();
            }
        }

        if(cmd.equals("buyProperty")){
            String property = parsed[1];
            long count = Long.parseLong(parsed[2]);
            long cost = getPropertyData(property);
            if(cost == 0){
                event.reply("파산된 항목은 구매 할 수 없습니다!").queue();
            }
            else {
                String uid = event.getUser().getId();
                long balance = getAccountBalance(uid);
                if(balance < count * cost){
                    event.reply("계좌의 잔액이 부족합니다!").queue();
                }
                else{
                    changeBalance(uid, balance - (count * cost));
                    changeUserProperty(uid, property, getOneProperty(uid, property) + count);
                    event.reply(event.getUser().getName() + " 의 부동산 거래 체결 완료!\n구매/" + property + "/" + parsed[2] + "채").queue();
                    event.getMessage().delete().queue();
                }
            }
        }
        if(cmd.equals("sellProperty")){
            String property = parsed[1];
            long count = Long.parseLong(parsed[2]);
            long cost = getPropertyData(property);
            String uid = event.getUser().getId();
            long balance = getAccountBalance(uid);
            if(count > getOneProperty(uid, property)){
                event.reply("보유한 부동산이 부족합니다!").queue();
            }
            else{
                changeBalance(uid, balance + (count * cost));
                changeUserProperty(uid, property, getOneProperty(uid, property) - count);
                event.reply(event.getUser().getName() + " 의 부동산 거래 체결 완료!\n판매/" + property + "/" + parsed[2] + "채").queue();
                event.getMessage().delete().queue();
            }
        }

        if(cmd.equals("makeNormalAccount")){
            String uid = event.getUser().getId();
            if(getAccountBalance(uid) != -1){
                event.reply("이미 개설된 계좌입니다!").queue();
            }
            else {
                System.out.println(event.getUser().getAvatarId());
                System.out.println(event.getUser().getName());
                makeAccount(uid);
                event.reply("자유입출금계좌 개설 완료!").queue();
            }
        }
    }

    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        String[] parsed = event.getComponentId().split("_");
        String cmd = parsed[0];
        if (parsed[0].equals("sendMoney")) {
            long cost = Long.parseLong(parsed[1]);
            // Mentions provide the selected values using familiar getters
            List<User> users = event.getMentions().getUsers();
            event.getMessage().delete().queue();
            int result = sendMonetToAccount(event.getUser().getId(), users.get(0).getId(), cost);
            if(result == -1){
                event.reply("송금 실패!\n사유: 개설되지 않은 계좌").queue();
            }
            if(result == -2){
                event.reply("송금 실패!\n사유: 잔액 부족").queue();
            }
            if(result == 0){
                event.reply("송금완료!\n" + event.getUser().getName() + " -> " +  users.get(0).getName() + ":" + parsed[1] + "₩").queue();
            }
            else{
                event.reply("송금 실패!\n사유: 기타사유(관리자에게 문의하세요)").queue();
            }
        }
    }
}
