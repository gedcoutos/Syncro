package com.syncro.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class MensagemGlobal {
    @ModelAttribute
    public void moverMensagens(HttpSession session, Model model) {
        if (session == null) return;
        Object erro = session.getAttribute("flashErro");
        if (erro != null) {
            model.addAttribute("erro", erro.toString());
            session.removeAttribute("flashErro");
        }
        Object ok = session.getAttribute("flashOk");
        if (ok != null) { model.addAttribute("ok", ok.toString()); session.removeAttribute("flashOk"); }
        Object info = session.getAttribute("flashInfo");
        if (info != null) { model.addAttribute("info", info.toString()); session.removeAttribute("flashInfo"); }
    }
}
